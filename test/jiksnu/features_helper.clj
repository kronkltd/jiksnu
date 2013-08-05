(ns jiksnu.features-helper
  (:use aleph.http
        aleph.formats
        [clj-factory.core :only [factory fseq]]
        clj-webdriver.taxi
        [clojure.core.incubator :only [-?>]]
        [jiksnu.mock :only [my-password]]
        [jiksnu.referrant :only [this that get-this get-that set-this set-that]]
        [lamina.core :only [permanent-channel read-channel* siphon]]
        midje.sweet
        ring.mock.request
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.config :as c]
            [ciste.core :as core]
            [ciste.model :as cm]
            [ciste.runner :as runner]
            [ciste.sections.default :as sections]
            [ciste.service.aleph :as aleph]
            [clj-webdriver.core :as webdriver]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            jiksnu.factory
            [jiksnu.model :as model]
            jiksnu.routes
            [jiksnu.session :as session]
            [ring.mock.request :as req])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(def server (atom nil))
(defonce loaded (atom false))

(defn after-hook
  []
  (try
    (log/info "after")
    (ciste.runner/stop-application!)
    (close)
    (catch Exception ex
      (log/error ex))))

(defn before-hook
  []
  (when-not @loaded
    (try
      (let [site-config (ciste.config/load-site-config)]
        (ciste.runner/start-application! :integration)
        (set-driver! {:browser
                      ;; :chrome
                      :firefox
                      ;; :htmlunit
                      })
        (ciste.loader/process-requires)
        (db/drop-all!)
        (.addShutdownHook
         (Runtime/getRuntime)
         (Thread. (fn [] (after-hook))))
        (dosync
         (ref-set this {})
         (ref-set that {})
         (ref-set my-password nil))
        (dosync
         (swap! loaded (constantly true))))
      (catch Exception ex
        (.printStackTrace ex)
        (System/exit 0)))))

