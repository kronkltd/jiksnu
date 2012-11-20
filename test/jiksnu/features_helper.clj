(ns jiksnu.features-helper
  (:use aleph.http
        aleph.formats
        [clj-factory.core :only [factory fseq]]
        clj-webdriver.taxi
        [clojure.core.incubator :only [-?>]]
        [jiksnu.existance-helpers :only [my-password]]
        [jiksnu.referrant :only [this that get-this get-that set-this set-that]]
        [lamina.core :only [permanent-channel read-channel* siphon]]
        [lamina.executor :only [task]]
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
            jiksnu.factory
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.routes
            [jiksnu.session :as session]
            [ring.mock.request :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.Domain
           jiksnu.model.User))

(def server (atom nil))

(defn before-hook
  []
  (try (let [site-config (ciste.config/load-site-config)]
         
         (ciste.runner/start-application! :integration)
         (set-driver! {:browser
                       ;; :firefox
                       :htmlunit})
         (ciste.loader/process-requires)
         (model/drop-all!)
         (dosync
          (ref-set this {})
          (ref-set that {})
          (ref-set my-password nil)))
       (catch Exception ex
         (.printStackTrace ex)
         (System/exit 0))))

(defn after-hook
  []
  (try
    (log/info "after")
    (ciste.runner/stop-application!)
    (catch Exception ex
      (log/error ex))))
