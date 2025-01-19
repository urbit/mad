(ns mad.data
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [toml.core :as toml]))

(def ec-urbit
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/u/urbit.toml")

(def ec-filecoin
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/f/filecoin.toml")

(def ec-near
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/n/near.toml")

;; top 20
(def ec-sui
  "")

(def ec-gnosis
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/g/gnosis.toml")

(def ec-chainlink
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/c/chainlink.toml")

;; top 40
(def ec-stellar
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/s/stellar.toml")

;; top 50
(def ec-eos
  "https://raw.githubusercontent.com/electric-capital/crypto-ecosystems/master/data/ecosystems/e/eos.toml")

(defn sub-ecosystem->raw-toml
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


(defn fetch-ec-repos
  "Utility used to bootstrap the set of repositories."
  [root-url project & [ecosystem]]
  (try
    (let [res   (-> (client/get root-url)
                    :body
                    toml/read)
          repos (format-ec-repos project
                                 (or ecosystem project)
                                 (get res "repo"))]
      (loop [repos repos
             sub-ecos (get res "sub_ecosystems")]
        (if (empty? sub-ecos)
          repos
          (recur
           (let [sub-eco (first sub-ecos)
                 sub-url (sub-ecosystem->raw-toml sub-eco)]
             (println
              (format "Fetching repos for subecosystem %s within project %s at %s:"
                      sub-eco project sub-url))
             (concat repos (fetch-ec-repos sub-url project sub-eco)))
           (rest sub-ecos)))))
    (catch Exception e
      (println e)
      [])))


;; TODO: ensure that electric capital's data is updated per the below, which has

;; deviated
(def known-repositories
  [
   {:owner "deelawn",
    :repo "urbit-gob",
    :added #inst "2024-08-02T04:48:52.327-00:00"}
   {:owner "narskidan" :repo "chatbot.hoon" :added #inst "2024-08-02T04:53:09.889-00:00"}
   {:owner "narskidan" :repo "roon" :added #inst "2024-08-02T04:53:09.889-00:00"}
   {:owner "nodsup-halnux" :repo "wui" :added #inst "2024-08-07T22:09:34.442-00:00"}
   {:owner "nodsup-halnux" :repo "Advanced-Hoon" :added #inst "2024-08-07T22:09:34.442-00:00"}
   {:owner "coeli-network" :repo "tyr" :added #inst "2024-08-02T04:53:09.889-00:00"}
   {:owner "IzaacMammadov" :repo "Hoon-Black-Scholes" :added #inst "2024-08-02T04:53:09.889-00:00"}
   {:owner "archetype-org" :repo "golem"}
   {:owner "lynko" :repo "urbit-names"}
   {:owner "lynko" :repo "re.hoon"}
   {:owner "lynko" :repo "holo-9000"}
   {:owner "dogsocean" :repo "speed-test"}
   {:owner "niblyx-malnus" :repo "Urbit-Monads-Tutorial"}
   {:owner "ajlamarc" :repo "urbit-defi"}
   {:owner "darighost" :repo "goals-bot"}
   {:owner "kitcat003" :repo "hoon-academy"}
   {:owner "mikolajpp" :repo "hoon-git"}
   {:owner "mikolajpp" :repo "bytestream" :added #inst "2024-08-02T04:53:09.889-00:00"}
   {:owner "bantus-follus" :repo "decadence"}
   {:owner "midden-fabler" :repo "phoenix-wip"}
   {:owner "midden-fabler" :repo "ahoy"}
   {:owner "midden-fabler" :repo "fleet"}
   {:owner "matthiasshaub" :repo "maat"}
   {:owner "wispem-wantex" :repo "urbit-uniswap"}
   {:owner "wispem-wantex" :repo "urbit-markdown"}
   {:owner "wispem-wantex" :repo "recipe-book"}
   {:owner "NEARBuilders" :repo "near-urbit-canvas"}
   {:owner "tiller-tolbus" :repo "chain"}
   {:owner "rahul-muster" :repo "urbitapi"}
   {:owner "irvdude" :repo "mydesk"}
   {:owner "arthyn" :repo "gin-tonic"}
   {:owner "deathtothecorporation" :repo "make"}
   {:owner "deathtothecorporation" :repo "mask"}
   {:owner "darighost" :repo "cen-chan"}
   {:owner "darighost" :repo "roon"}
   {:owner "Anseta" :repo "urbit-tea"}
   {:owner "archetype-org" :repo "clack"}
   {:owner "youfoundron" :repo "urbit-academy"}
   {:owner "sidnym-ladrut" :repo "camp"}
   {:owner "sidnym-ladrut" :repo "elogin"}
   {:owner "jamesacklin" :repo "urbit-webhook"}
   {:owner "paglud-nodsyn" :repo "oracle"}
   {:owner "bohendo" :repo "bizbaz"}
   {:owner "Quodss" :repo "wasm-hackathon"}
   {:owner "Quodss"
    :repo "urwasm"
    :branches ["lia"]}
   {:owner "walter-hoddel" :repo "mail"}
   {:owner "supercoolyun" :repo "grip"}
   {:owner "supercoolyun" :repo "grip-sample"}
   {:owner "namful-mocwen" :repo "yijing"}
   {:owner "dislux-hapfyl" :repo "h00nxfu"}
   {:owner "salbaroudi" :repo "Quoridor"}
   {:owner "urbitme" :repo "urbit-ob-rs"}
   {:owner "urbitme" :repo "sigil-phonemes"}
   {:owner "tocwex" :repo "fund"}
   {:owner "dogsoceans" :repo "ama"}
   {:owner "dogsoceans" :repo "foes"}
   {:owner "dogsoceans" :repo "friend-adder3"}
   {:owner "dogsoceans" :repo "prism"}
   {:owner "dogsoceans" :repo "-urspace"}
   {:owner "dogsoceans" :repo "-onion"}
   {:owner "dogsoceans" :repo "simple-notes"}
   {:owner "dogsoceans" :repo "rudpage"}
   {:owner "dogsoceans" :repo "roon"}
   {:owner "charlieroth" :repo "hoon-school"}
   {:owner "elliotBraem" :repo "near-urbit-tldraw"}
   {:owner "yungcalibri" :repo "pharos"}
   {:owner "sidnym-ladrut" :repo "urbitswap"}
   {:owner "urbitswap" :repo "urbitswap"}
   {:owner "bantus-follus" :repo "schizohost"}
   {:owner "Native-Planet" :repo "onyx"}
   {:owner "fischsauce" :repo "delta"}
   {:owner "dmvianna" :repo "delta"}
   {:owner "neonfuz" :repo "urlingo"}
   {:owner "ngzax" :repo "urbit-sync"}
   {:owner "JohnRillos" :repo "wiki"}
   {:owner "Native-Planet" :repo "penpAI"}
   {:owner "Native-Planet" :repo "aqmon"}
   {:owner "patosullivan" :repo "trackur"}
   {:owner "salbaroudi" :repo "Quoridor"}
   {:owner "pilwex-migsun" :repo "torb"}
   {:owner "ryjm" :repo "boat"}
   {:owner "fischsauce" :repo "app-school"}
   {:owner "ilyakooo0" :repo "seax"}
   {:owner "nallux-dozryl" :repo "nock"}
   {:owner "bohendo" :repo "hsl"}
   {:owner "chrisalexadams" :repo "collective"}
   {:owner "LaconicNetwork" :repo "awesome-urbit"}
   {:owner "structure-group" :repo "albums"}
   {:owner "gmcz" :repo "cals"}
   {:owner "holium" :repo "desks"}
   {:owner "holium" :repo "realm"}
   {:owner "latter-bolden" :repo "pier-transfer"}
   {:owner "xywei" :repo "uforth"}
   {:owner "JohnRillos" :repo "urbit_template"}
   {:owner "urbit-pilled" :repo "hoon-ts-editors"}
   {:owner "bonbud-macryg" :repo "rss-sub"}
   {:owner "mbcladwell" :repo "gurbit"}
   {:owner "yungcalibri" :repo "prism"}
   {:owner "altugbakan" :repo "hsl"}
   {:owner "bzol" :repo "cosmos"}
   {:owner "bzol" :repo "cosmos"}
   {:owner "mnrrxyz" :repo "HSL"}
   {:owner "pasnec-salmyr" :repo "hsl"}
   {:owner "parkbanks" :repo "hsl"}
   {:owner "tamlut-modnys" :repo "curriculum"}
   {:owner "JR-Vickers" :repo "hoon_school_exercises"}
   {:owner "jpfeiffer16" :repo "hoonschool"}
   {:owner "mbcladwell" :repo "bookstore"}
   {:owner "sidnym-ladrut" :repo "desk"}
   {:owner "bzol" :repo "hitler"}
   {:owner "salbaroudi" :repo "HoonSchool_06.2023"}
   {:owner "urbit-pilled" :repo "tree-sitter-hoon"}
   {:owner "urbit-pilled" :repo "vscode-hoon"}
   {:owner "toddux-dopnel" :repo "excalidraw-for-urbit"}
   {:owner "tamlut-modnys" :repo "yijingbot"}
   {:owner "tamlut-modnys" :repo "userspace-util"}
   {:owner "ashelkovnykov", :repo "urbit-hosting"}
   {:owner "forchesta", :repo "urbit-dev"}
   {:owner "liam-fitzgerald", :repo "urbit-openai"}
   {:owner "matthew-levan", :repo "tell"}
   {:owner "matthiasschaub", :repo "tahuti"}
   {:owner "matthiasschaub", :repo "pilothouse"}
   {:owner "mrdomino" :repo "urbit-sysops"}
   {:owner "nodreb-borrus", :repo "sigil-render"}
   {:owner "hanryc-tirpur", :repo "trail-mobile"}
   {:owner "hanryc-tirpur", :repo "trail"}
   {:owner "cyclomancer", :repo "bet"}
   {:owner "cyclomancer", :repo "ccrur"}
   {:owner "cyclomancer", :repo "volt"}
   {:owner "worpet-bildet", :repo "portal"}
   {:owner "dalten-collective", :repo "agent-skeleton"}
   {:owner "dalten-collective", :repo "books"}
   {:owner "dalten-collective", :repo "cgol"}
   {:owner "dalten-collective", :repo "channel"}
   {:owner "dalten-collective", :repo "color"}
   {:owner "dalten-collective", :repo "crow"}
   {:owner "dalten-collective", :repo "erth"}
   {:owner "dalten-collective", :repo "expo"}
   {:owner "dalten-collective", :repo "full-stop"}
   {:owner "dalten-collective", :repo "glue"}
   {:owner "dalten-collective", :repo "gora"}
   {:owner "dalten-collective", :repo "ibis"}
   {:owner "dalten-collective", :repo "keep"}
   {:owner "dalten-collective", :repo "orca"}
   {:owner "dalten-collective", :repo "peat"}
   {:owner "dalten-collective", :repo "pick"}
   {:owner "dalten-collective", :repo "prod"}
   {:owner "dalten-collective", :repo "schooner"}
   {:owner "dalten-collective", :repo "simple-app"}
   {:owner "dalten-collective", :repo "trove"}
   {:owner "dalten-collective", :repo "ucal"}
   {:owner "dalten-collective", :repo "upfs"}
   {:owner "dalten-collective", :repo "wrdu"}
   {:owner "dalten-collective", :repo "zlack"}
   {:owner "dalten-collective", :repo "grove"}
   {:owner "dalten-collective", :repo "aviary"}
   {:owner "dr-frmr", :repo "scan"}
   {:owner "dr-frmr", :repo "pokur"}
   {:owner "Fang-", :repo "suite"}
   {:owner "gusmacaulay", :repo "atlas"}
   {:owner "gusmacaulay", :repo "dukebox-glob"}
   {:owner "gusmacaulay", :repo "mentat"}
   {:owner "h5gq3", :repo "graph-query"}
   {:owner "hanfel-dovned", :repo "Bless"}
   {:owner "hanfel-dovned", :repo "Click"}
   {:owner "hanfel-dovned", :repo "Feature"}
   {:owner "hanfel-dovned", :repo "Slam"}
   {:owner "hanfel-dovned", :repo "Pony"}
   {:owner "hanfel-dovned", :repo "Board"}
   {:owner "holium", :repo "ballot"}
   {:owner "holium", :repo "bounties"}
   {:owner "holium", :repo "campfire"}
   {:owner "holium", :repo "design-system"}
   {:owner "holium", :repo "engram"}
   {:owner "holium", :repo "hiring-frontend"}
   {:owner "holium", :repo "os-sandbox"}
   {:owner "holium", :repo "realm-docs"}
   {:owner "holium", :repo "realm-support"}
   {:owner "holium", :repo "trove"}
   {:owner "holium", :repo "webrtc-dev-server"}
   {:owner "holium", :repo "tome-db"}
   {:owner "hosted-fornet", :repo "crunch"}
   {:owner "hosted-fornet", :repo "ursr"}
   {:owner "hosted-fornet", :repo "whitelist"}
   {:owner "jackfoxy", :repo "urQL"}
   {:owner "johnhyde", :repo "astrolabe"}
   {:owner "johnhyde", :repo "turf"}
   {:owner "johnhyde", :repo "monkey"}
   {:owner "R-JG", :repo "homunculus"}
   {:owner "R-JG", :repo "mast"}
   {:owner "midlyx-hatrys", :repo "urbit-cob"}
   {:owner "midsum-salrux", :repo "faux"}
   {:owner "midsum-salrux", :repo "gato"}
   {:owner "midsum-salrux", :repo "tendiebot"}
   {:owner "mopfel-winrux", :repo "sand"}
   {:owner "mopfel-winrux", :repo "NockPU"}
   {:owner "mopfel-winrux", :repo "urbit-umbrel"}
   {:owner "Native-Planet", :repo "anchor"}
   {:owner "Native-Planet", :repo "anchor-source"}
   {:owner "Native-Planet", :repo "GroundSeg"}
   {:owner "Native-Planet", :repo "urbit-casaos"}
   {:owner "Native-Planet", :repo "uart-agent"}
   {:owner "Native-Planet", :repo "urbit-docker"}
   {:owner "niblyx-malnus", :repo "nested-goals"}
   {:owner "niblyx-malnus", :repo "surf"}
   {:owner "niblyx-malnus", :repo "goals"}
   {:owner "niblyx-malnus", :repo "clibox"}
   {:owner "niblyx-malnus", :repo "login"}
   {:owner "Other-Life", :repo "page"}
   {:owner "robkorn", :repo "urbit-content-archiver"}
   {:owner "ryjm", :repo "citadel"}
   {:owner "ryjm", :repo "srrs"}
   {:owner "orlin" :repo "urbit-desk-thing"}
   {:owner "j3-productions", :repo "quorum"}
   {:owner "sidnym-ladrut", :repo "pantheon"}
   {:owner "sigilante", :repo "atalante"}
   {:owner "sigilante", :repo "l10n"}
   {:owner "sigilante", :repo "lazytrig"}
   {:owner "sigilante", :repo "mush"}
   {:owner "sigilante", :repo "chronos"}
   {:owner "sigilante", :repo "rpn"}
   {:owner "sigilante" :repo "just" :added #inst "2024-08-02T04:54:29.826-00:00"}
   {:owner "sigilante" :repo "seek" :added #inst "2024-08-02T04:54:29.826-00:00"}
   {:owner "taalhavras", :repo "ucal"}
   {:owner "Tenari", :repo "lift"}
   {:owner "Tenari", :repo "urbit-heroes"}
   {:owner "Tenari", :repo "ur-recipes"}
   {:owner "Tenari", :repo "ur-tasks"}
   {:owner "thecommons-urbit", :repo "chess"}
   {:owner "litlep-nibbyt", :repo "forums"}
   {:owner "tinnus-napbus", :repo "base16-hoon"}
   {:owner "tinnus-napbus", :repo "files"}
   {:owner "tinnus-napbus", :repo "beacon"}
   {:owner "tinnus-napbus", :repo "docs-app"}
   {:owner "tinnus-napbus", :repo "sentinel"}
   {:owner "tinnus-napbus", :repo "straw"}
   {:owner "tirrel-corp", :repo "scene"}
   {:owner "tirrel-corp", :repo "studio"}
   {:owner "tloncorp", :repo "arigato"}
   {:owner "tloncorp", :repo "bouncer"}
   {:owner "tloncorp", :repo "hoon-vscode"}
   {:owner "tloncorp", :repo "landscape-apps"}
   {:owner "tloncorp", :repo "tlon-apps"}
   {:owner "tloncorp", :repo "landscape"}
   {:owner "tloncorp", :repo "mock-http-api"}
   {:owner "tloncorp", :repo "eyrie"}
   {:owner "tloncorp", :repo "hoon-format"}
   {:owner "urbit" :repo "urcrypt-sys"}
   {:owner "urbit" :repo "docs.urbit.org"}
   {:owner "urbit" :repo "hits"}
   {:owner "urbit" :repo "urbit.foundation"}
   {:owner "urbit" :repo "UIPs"}
   {:owner "urbit" :repo "NEAR"}
   {:owner "urbit" :repo "create-near-app"}
   {:owner "urbit" :repo "NearSocialVM"}
   {:owner "urbit" :repo "Aegean"}
   {:owner "urbit", :repo "id.urbit.org"}
   {:owner "urbit", :repo "antechamber"}
   {:owner "urbit", :repo "archaeology"}
   {:owner "urbit", :repo "archaeology-factor"}
   {:owner "urbit", :repo "archaeology2"}
   {:owner "urbit", :repo "arvo"}
   {:owner "urbit", :repo "aura-js"}
   {:owner "urbit", :repo "awesome-urbit"}
   {:owner "urbit", :repo "azimuth"}
   {:owner "urbit", :repo "azimuth-cairo"}
   {:owner "urbit", :repo "azimuth-hs"}
   {:owner "urbit", :repo "azimuth-js"}
   {:owner "urbit", :repo "babysit"}
   {:owner "urbit", :repo "bitcoin-wallet"}
   {:owner "urbit", :repo "bootlegs"}
   {:owner "urbit", :repo "bridge"}
   {:owner "urbit", :repo "bridge-libs"}
   {:owner "urbit", :repo "chess"}
   {:owner "urbit", :repo "commonmark-legacy"}
   {:owner "urbit", :repo "create-landscape-app"}
   {:owner "urbit", :repo "developers.urbit.org"}
   {:owner "urbit", :repo "docs"}
   {:owner "urbit", :repo "docs-examples"}
   {:owner "urbit", :repo "examples"}
   {:owner "urbit", :repo "fleet"}
   {:owner "urbit", :repo "fora"}
   {:owner "urbit", :repo "fora-posts"}
   {:owner "urbit", :repo "foundation"}
   {:owner "urbit", :repo "foundation-design-system"}
   {:owner "urbit", :repo "garden"}
   {:owner "urbit", :repo "grants"}
   {:owner "urbit", :repo "gsoc-2015-ideas"}
   {:owner "urbit", :repo "hoon-assist"}
   {:owner "urbit", :repo "hoon-assist-vscode"}
   {:owner "urbit", :repo "hoon-language-server"}
   {:owner "urbit", :repo "hoon-LT"}
   {:owner "urbit", :repo "hoon-mode.el"}
   {:owner "urbit", :repo "hoon-workbook"}
   {:owner "urbit", :repo "hoon.vim"}
   {:owner "urbit", :repo "hoonschool"}
   {:owner "urbit", :repo "http-parser-legacy"}
   {:owner "urbit", :repo "inbox"}
   {:owner "urbit", :repo "indigo-dark"}
   {:owner "urbit", :repo "indigo-light"}
   {:owner "urbit", :repo "indigo-react"}
   {:owner "urbit", :repo "indigo-static"}
   {:owner "urbit", :repo "indigo-tokens"}
   {:owner "urbit", :repo "interface"}
   {:owner "urbit", :repo "io_drivers"}
   {:owner "urbit", :repo "jaque-fresh"}
   {:owner "urbit", :repo "landscape"}
   {:owner "urbit", :repo "language-hoon"}
   {:owner "urbit", :repo "ledger"}
   {:owner "urbit", :repo "lure"}
   {:owner "urbit", :repo "network-api"}
   {:owner "urbit", :repo "network-api-deprecated"}
   {:owner "urbit", :repo "network-explorer"}
   {:owner "urbit"
    :repo "ares"
    :branches ["eamsden/codegen" "eamsden/codegen-old" "msl/codegen"]}
   {:owner "urbit"
    :repo "shrub"
    :branches ["develop"]}
   {:owner "urbit", :repo "nock-js"}
   {:owner "urbit", :repo "nockjs"}
   {:owner "urbit", :repo "noun"}
   {:owner "urbit", :repo "old-doc"}
   {:owner "urbit", :repo "old-urbit.org"}
   {:owner "urbit", :repo "operators.urbit.org"}
   {:owner "urbit", :repo "PaperRenderer"}
   {:owner "urbit", :repo "phonemes-js"}
   {:owner "urbit", :repo "plan"}
   {:owner "urbit", :repo "pleac-hoon"}
   {:owner "latter-bolden", :repo "port"}
   {:owner "urbit", :repo "pottery"}
   {:owner "urbit", :repo "preview"}
   {:owner "urbit", :repo "profile"}
   {:owner "urbit", :repo "proposals"}
   {:owner "urbit", :repo "repl"}
   {:owner "urbit", :repo "roadmap.urbit.org"}
   {:owner "urbit", :repo "roller-rpc-client"}
   {:owner "urbit", :repo "runner-js"}
   {:owner "urbit", :repo "rust-mixed-with-c"}
   {:owner "urbit", :repo "safe-cli"}
   {:owner "urbit", :repo "shop.urbit.org"}
   {:owner "urbit", :repo "sigil-figma-plugin"}
   {:owner "urbit", :repo "sigil-js"}
   {:owner "urbit", :repo "skeleton"}
   {:owner "urbit", :repo "sniproxy"}
   {:owner "urbit", :repo "sole"}
   {:owner "urbit", :repo "star.market"}
   {:owner "urbit", :repo "starketplace"}
   {:owner "urbit", :repo "support"}
   {:owner "urbit", :repo "tree"}
   {:owner "urbit", :repo "up8-ticket"}
   {:owner "urbit", :repo "urb"}
   {:owner "urbit",
    :repo "urbit"
    :branches ["develop" "lf/neo" "yu/sema" "jb/plot" "yu/sema-mesa"
               "next/kelvin/410"]}
   {:owner "urbit"
    :repo "vere"
    :branches ["develop" "jb/plot" "lf/xmas" "yu/mesa" "next/kelvin/410"]}
   {:owner "urbit", :repo "urbit-bitcoin-rpc"}
   {:owner "urbit", :repo "urbit-hob"}
   {:owner "urbit", :repo "urbit-key-generation"}
   {:owner "urbit", :repo "urbit-ob"}
   {:owner "urbit", :repo "urbit-wallet-generator"}
   {:owner "urbit", :repo "urbit-webrtc"}
   {:owner "urbit", :repo "urbit.org"}
   {:owner "urbit", :repo "urbit_book"}
   {:owner "urbit", :repo "volt"}
   {:owner "urbit", :repo "watch"}
   {:owner "urbit", :repo "web-repl"}
   {:owner "urbit", :repo "womb"}
   {:owner "urbit", :repo "work-app"}
   {:owner "urbit", :repo "write"}
   {:owner "urbit", :repo "js-http-api"}
   {:owner "urbit", :repo "tools"}
   {:owner "Vrend", :repo "urbit-go"}
   {:owner "yosoyubik", :repo "canvas"}
   {:owner "tomholford" :repo "hodl"}
   {:owner "JohnRillos" :repo "whom"}
   {:owner "nogira" :repo "udm"}
   {:owner "Demonstrandum" :repo "AdventusMMXXII"}
   {:owner "ravern" :repo "hoons"}
   {:owner "yosoyubik" :repo "hoonschool"}
   {:owner "yosoyubik" :repo "advent-of-hoon"}
   {:owner "hjorthjort" :repo "knock"}
   {:owner "mbcladwell" :repo "urblib"}
   {:owner "JR-Vickers" :repo "hoonSchoolExercises"}
   {:owner "ajlamarc" :repo "racket"}
   {:owner "ajlamarc" :repo "hue"}
   {:owner "tadad" :repo "blog"}
   {:owner "tadad" :repo "blog-ui"}
   {:owner "polrel-witter" :repo "gather"}
   {:owner "polrel-witter" :repo "live"}
   {:owner "brbenji" :repo "focus"}
   {:owner "brbenji" :repo "news"}
   {:owner "tiller-tolbus" :repo "cgol"}
   {:owner "divergio" :repo "realtime"}
   {:owner "bacwyls" :repo "radio"}
   {:owner "bacwyls" :repo "basket"}
   {:owner "bacwyls" :repo "jukebox"}
   {:owner "assemblycapital" :repo "vita"}
   {:owner "assemblycapital" :repo "houston"}
   {:owner "MarcusMiguel" :repo "classifieds"}
   {:owner "uqbar-dao", :repo "dao-contract"}
   {:owner "uqbar-dao", :repo "escape-app"}
   {:owner "uqbar-dao", :repo "galaxy-girls"}
   {:owner "uqbar-dao", :repo "indexer-ui"}
   {:owner "uqbar-dao", :repo "network-age"}
   {:owner "uqbar-dao", :repo "phonebook"}
   {:owner "uqbar-dao", :repo "rdbms"}
   {:owner "uqbar-dao", :repo "nectar"}
   {:owner "uqbar-dao", :repo "sirens-of-uqbar"}
   {:owner "uqbar-dao", :repo "react-native-api"}
   {:owner "uqbar-dao", :repo "ui-components"}
   {:owner "uqbar-dao", :repo "uqbar-core"}
   {:owner "uqbar-dao", :repo "urbit-mobile-app-template"}
   {:owner "uqbar-dao", :repo "urbit-ui-template"}
   {:owner "uqbar-dao", :repo "wallet-ui"}
   {:owner "uqbar-dao", :repo "ziggurat"}
   {:owner "uqbar-dao", :repo "ziggurat-ui"}
   {:owner "uqbar-dao", :repo "pongo"}
   {:owner "uqbar-dao", :repo "pongo-app"}
   {:owner "uqbar-dao", :repo "fire-hose"}
   {:owner "uqbar-dao", :repo "dev-suite"}
   {:owner "uqbar-dao", :repo "screed"}
   ])
