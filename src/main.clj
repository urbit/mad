(ns mad.main
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as sqlh]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.instant :as instant]
            [java-time.api :as jt]
            [toml.core :as toml]))

(def db
  {:dbtype "postgres"
   :dbname "udev"})

(def ds (jdbc/get-datasource db))

(def github-auth-token
  "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")

(defn create-tables! [ds]
  (do
    (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS commit (
    commit_id SERIAL PRIMARY KEY,
    sha TEXT UNIQUE NOT NULL,
    owner TEXT NOT NULL,
    project TEXT NOT NULL,
    ecosystem TEXT NOT NULL,
    repo TEXT NOT NULL,
    author TEXT NOT NULL,
    date TIMESTAMPTZ NOT NULL,
    message TEXT
  )"])
    (jdbc/execute! ds ["
 CREATE TABLE IF NOT EXISTS repository (
    repo_id SERIAL PRIMARY KEY,
    project TEXT NOT NULL,
    ecosystem TEXT NOT NULL,
    owner TEXT NOT NULL,
    repo TEXT NOT NULL,
    blacklist BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (project, ecosystem, owner, repo)
)"])))

(defn drop-tables! [ds]
  (do
    (jdbc/execute! ds ["DROP TABLE IF EXISTS commit"])
    (jdbc/execute! ds ["DROP TABLE IF EXISTS repository"])))

(defn reset-db! [ds]
  (do
    (drop-tables! ds)
    (create-tables! ds)))

(defn parse-commits
  [project ecosystem owner repo response]
  (update response :body
          (fn [body]
            (->> (map (comp
                       (fn [{:keys [committer message tree author]}]
                         {:author    (:name author)
                          :message   message
                          :repo      repo
                          :project   project
                          :ecosystem ecosystem
                          :owner     owner
                          :date      (jt/instant->sql-timestamp (jt/instant (:date committer)))
                          :sha       (:sha tree)})
                       :commit)
                      body)))))

(defn fetch-commits
  [url & {:keys [per-page] :or {per-page 100}}]
  (-> (client/get url
                  {:content-type :json
                   :query-params {:per_page (str per-page)}
                   :headers      {:Authorization (str "Bearer " github-auth-token)}})
      (update :body #(json/read-str % :key-fn keyword))))

(defn has-commit?
  "Are `commits` already in the database?"
  [{sha :sha :as commit}]
  (seq (jdbc/execute! ds
                      (-> (sqlh/select :sha)
                          (sqlh/from :commit)
                          (sqlh/where [:= :sha sha])
                          sql/format))))

(defn ecosystem->uri
  [sub]
  (let [c (clojure.string/lower-case (first sub))]
    (format "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/%s/%s.toml"
            c
            (-> sub
                clojure.string/lower-case
                (clojure.string/replace "." "-")
                (clojure.string/replace " " "-")))))

(defn extract-owner-repo [s]
  (let [[owner repo]
        (->> (clojure.string/split s #"/")
             (take-last 2))]
    {:owner owner :repo repo}))

(defn- format-ec-repos
  [project ecosystem repos]
  (map (comp #(assoc % :project project :ecosystem ecosystem)
             extract-owner-repo
             #(get % "url"))
       repos))

(defn fetch-ec-repos*
  [root-url project & [ecosystem]]
  (try
    (let [res      (-> (client/get root-url)
                       :body
                       toml/read)
          repos    (format-ec-repos project
                                    (or ecosystem project)
                                    (get res "repo"))]
      (loop [repos    repos
             sub-ecos (get res "sub_ecosystems")]
        (if (empty? sub-ecos)
          repos
          (recur
           (let [sub-eco (first sub-ecos)
                 sub-url (ecosystem->uri sub-eco)]
             (println
              (format ">> REPOSITORIES: fetch %s|%s"
                      project sub-eco))
             (concat repos (fetch-ec-repos* sub-url project sub-eco)))
           (rest sub-ecos)))))
    (catch Exception e
      (println e)
      [])))

(defn fetch-ec-repos
  "Fetch all repositories for the given `project` and recursively fetch all sub-ecosystems.."
  [project]
  (println (format "> REPOSITORIES: fetch %s" project))
  (fetch-ec-repos* (ecosystem->uri project) project))

(defn ingest-new-commits!
  [{:keys [repository/project
           repository/ecosystem
           repository/owner
           repository/repo
           repository/blacklist] :as r}
   & {:keys [delay branch] :or {delay 0}}]
  (when-not blacklist
    (println (format "> COMMITS: %s|%s:%s/%s"
                     project ecosystem owner repo))
    (loop [url (if branch
                 (format "http://api.github.com/repos/%s/%s/commits?sha=%s" owner repo branch)

                 (format "http://api.github.com/repos/%s/%s/commits" owner repo))]
      (let [{:keys [body links]}
            (parse-commits project ecosystem owner repo (fetch-commits url))]
        (jdbc/execute! ds (-> (sqlh/insert-into :commit)
                              (sqlh/values body)
                              (sqlh/upsert (-> (sqlh/on-conflict :sha)
                                               (sqlh/do-nothing)))
                              (sqlh/returning :*)
                              sql/format))
        (if-let [next (and (not (has-commit? (last body))) (:next links))]
          (do
            (println (format ">> COMMITS: %s/%s at url: %s\n"
                             owner repo next))
            (when (> delay 0)
              (Thread/sleep delay))
            (recur (:href next)))
          :done)))))


(defn ingest-repositories!
  [repositories]
  (let [result (jdbc/execute! ds (-> (sqlh/insert-into :repository)
                                     (sqlh/values repositories)
                                     (sqlh/upsert (-> (sqlh/on-conflict :owner :repo :project :ecosystem)
                                                      (sqlh/do-nothing)))
                                     (sqlh/returning :*)
                                     sql/format))]
    (println (format "REPOSITORIES: wrote %s" (count result)))
    :done))

(comment

  (ingest-repositories!
   (mapv
    (fn [{:keys [owner repo]}]
      {:repository/owner owner
       :repository/repo repo
       :repository/project "urbit"
       :repository/ecosystem "urbit"
       :repository/blacklist false})
    mad.data/known-repositories))

  (refresh-project! "urbit" {})

  (ingest-new-commits!
   {:repository/project "urbit"
    :repository/ecosystem "urbit"
    :repository/owner "urbit"
    :repository/repo "urbit"})

  (ingest-new-commits!
   {:repository/project "urbit"
    :repository/ecosystem "urbit"
    :repository/owner "urbit"
    :repository/repo "vere"})

  )

(defn refresh-project!
  [project {:keys [refresh-repos? delay]
            :or   {refresh-repos? false
                   delay          0}}]
  (when refresh-repos?
    (ingest-repositories! (fetch-ec-repos project)))
  (let [repositories
        (jdbc/execute! ds (-> (sqlh/select :*)
                              (sqlh/from :repository)
                              (sqlh/where [:= :project project])
                              sql/format))]
    (doseq [r repositories]
      (try
        (ingest-new-commits! r :delay delay)
        (catch Exception e
          (println e))))))

(defn refresh-projects!
  [& {:as opts}]
  (doseq [{project :repository/project}
          (jdbc/execute! ds (-> (sqlh/select-distinct :project)
                                (sqlh/from :repository)
                                (sql/format)))]
    (refresh-project! project opts)))

;; OPTIMIZATIONS TO MAKE
;; x stop fetching commits when last commit is already in DB
;;   x verify that commits are not in order of descending time
;; x store repositories in table
;;   x add column to blacklist a repository
;; x separate repo fetching and commit fetching into separate steps
;; * create blacklists

(def all-projects
  ["cardano"
   "polygon"
   "chainlink"
   "filecoin"
   "starknet"
   "internet-computer"
   "aptos"
   "near"
   "optimism"
   "avalanche"
   "solana"])

(defn load-all-repositories!
  [& [projects]]
  (doseq [project (or projects all-projects)]
    (doseq [rs (partition 1000 1000 nil (fetch-ec-repos project))]
      (ingest-repositories! rs))))

(comment

  ;; initialize database
  ;; (reset-db! ds)

  ;; load all repositories into db
  (load-all-repositories!)

  (refresh-projects! :delay 1500)

  (refresh-project! "urbit" {})

  (do
    (refresh-project! "internet-computer" {:delay 2000})
    (refresh-project! "starknet" {:delay 2000})))
