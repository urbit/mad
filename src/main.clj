(ns mad.main
  (:require [mad.data :as data]
            [next.jdbc :as jdbc]
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
    owner TEXT NOT NULL,
    repo  TEXT NOT NULL
)"])))


(defn drop-tables! [ds]
  (do
    (jdbc/execute! ds ["DROP TABLE IF EXISTS commit"])
    (jdbc/execute! ds ["DROP TABLE IF EXISTS repository"])))


(defn reset-db! [ds]
  (do
    (drop-tables! ds)
    (create-tables! ds)))


(defn fetch-commits [url owner repo project ecosystem]
  (let [{:keys [body links] :as result}
        (-> (client/get url {:content-type :json
                             :query-params {:per_page "100"}
                             :headers {:Authorization (str "Bearer " github-auth-token)}})
            (select-keys [:body :links])
            (update :body #(json/read-str % :key-fn keyword)))]
    (update result :body
            (fn [body]
              (->> (map (comp
                         (fn [{:keys [committer message tree author]}]
                           {:author (:name author)
                            :message message
                            :repo repo
                            :project project
                            :ecosystem ecosystem
                            :owner owner
                            :date (jt/instant->sql-timestamp (jt/instant (:date committer)))
                            :sha (:sha tree)})
                         :commit)
                        body))))))


(defn fetch-and-write-commits! [ds owner repo project ecosystem]
  (loop [url (format "http://api.github.com/repos/%s/%s/commits" owner repo)]
    (let [{:keys [body links]}
          (fetch-commits url owner repo project ecosystem)]
      (jdbc/execute! ds (-> (sqlh/insert-into :commit)
                            (sqlh/values body)
                            (sqlh/upsert (-> (sqlh/on-conflict :sha)
                                             (sqlh/do-nothing)))
                            (sqlh/returning :*)
                            sql/format))
      (if-let [next (:next links)]
        (do
          (println url)
          (recur (:href next)))
        :done))))

(comment

  ;; initialize database
  (reset-db! ds)

  ;; load all urbit repositories into db
  (doseq [{:keys [owner repo]} (reverse data/known-repositories)]
    (try (fetch-and-write-commits! ds owner repo "urbit" "urbit")
         (catch Exception e
           (println e))))

  )
