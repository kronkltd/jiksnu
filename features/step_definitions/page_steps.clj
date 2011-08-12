(use 'jiksnu.http)
(use 'jiksnu.model)
(use 'clj-factory.core)
(use 'ring.mock.request)
(import 'jiksnu.model.Activity)
(require 'jiksnu.model.activity)
(require 'ciste.config)
(use 'aleph.http)
(use 'aleph.formats)
(use 'clojure.test)

(def server (atom nil))
(def current-page (ref nil))

(Before
  (dosync
   (with-environment :test
     (ciste.config/load-config)
     (println "Starting server")
     (reset! server (with-environment :test (start 8085))))))

(After
  (println "Going down")
  (println @server)
  (@server)
  (println "shutting down agents")
  (shutdown-agents)
  (println "after is done"))


(Given #"the user is not logged in"
  (fn []
    (Thread/sleep 3000)
    (println "done sleeping")))

(Given #"an? activity exists"
  (fn []
    (with-environment :test
      (println (jiksnu.model.activity/create (factory Activity))))))

(When #"I visit the home page"
  (fn []
    (let [request {:method :get
                   :url "http://localhost:8085/"}
          response (sync-http-request request)]
      (dosync
       (ref-set current-page response)))))

(Then #"I should see an activity"
  (fn []
    (let [response @current-page
          body (channel-buffer->string (:body response))]
      (is (re-matches #".*hentry.*" body)))))

(Then #"I should see a list of activities"
  (fn []
    (identity nil)))
