(ns jiksnu.features-helper
  (:require [clj-webdriver.taxi :as taxi]
            [taoensso.timbre :as timbre]
            [jiksnu.db :as db]
            jiksnu.factory
            [jiksnu.mock :refer [my-password]]
            [jiksnu.referrant :refer [this that set-this set-that]]
            [slingshot.slingshot :refer [throw+ try+]]))

(def server (atom nil))
(defonce loaded (atom false))

(defn after-hook
  []
  (try+
   (timbre/info "after")
   (ciste.runner/stop-application!)
   (taxi/close)
   (catch Throwable ex
     (timbre/error ex))))

(defn before-hook
  []
  (when-not @loaded
    (try+
     (let [site-config (ciste.config/load-site-config)]
       (ciste.runner/start-application! :integration)
       (taxi/set-driver! {:browser
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
     (catch Throwable ex
       (.printStackTrace ex)
       (System/exit 0)))))
