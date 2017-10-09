(ns jiksnu.helpers.features
  (:require [clj-webdriver.driver :as driver]
            [clj-webdriver.taxi :as taxi]
            [jiksnu.mock :refer [my-password]]
            [jiksnu.referrant :refer [this that set-this set-that]]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre])
  (:import (org.openqa.selenium.remote DesiredCapabilities CapabilityType RemoteWebDriver)
           (java.net URL)))

(def server (atom nil))
(defonce loaded (atom false))
(def driver (atom nil))

(defn get-selenium-config
  []
  (let [host "selenium"
        port 24444
        url (str "http://" host ":" port "/wd/hub")]
    {:host host
     :port port
     :url url}))

(defn restart-session
  []
  (timbre/info "Checking session")
  (when (not @driver)
    (timbre/info "Starting session")
    (let [{:keys [url]} (get-selenium-config)
          caps (doto (DesiredCapabilities.)
                 (.setCapability CapabilityType/BROWSER_NAME "firefox")
                 (.setCapability "name" "clj-webdriver-test-suite"))
          wd (RemoteWebDriver. (URL. url) caps)]
      (timbre/debug "Getting connection")
      (let [session-id (str (.getSessionId wd))]
        (timbre/infof "Session Id: %s" session-id)
        (reset! driver wd)))))

(defn after-hook
  []
  (timbre/info "Running after hook")
  (taxi/quit))

(defn before-hook
  []
  (when-not @loaded
    (try+
     (restart-session)
     (-> @driver driver/init-driver taxi/set-driver!)
     (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. (fn []
                 (timbre/info "Running shutdown hook")
                 (after-hook))))
     (dosync
      (ref-set this {})
      (ref-set that {})
      (ref-set my-password nil))
     (dosync
      (swap! loaded (constantly true)))
     (catch Throwable ex
       (timbre/error ex "Before hooks failed")
       (System/exit 0)))))
