(use 'aleph.http)
(use 'aleph.formats)
(use '[clj-factory.core :only (factory)])
(use 'clojure.test)
(use 'jiksnu.http)
(use 'jiksnu.model)
(use 'midje.sweet)
(use 'ring.mock.request)
(require '[ciste.config :as c])
(require '[jiksnu.session :as session])
(require '[jiksnu.actions.activity-actions :as actions.activity])
(require '[jiksnu.actions.user-actions :as actions.user])
(require '[jiksnu.model.user :as model.user])
(import 'jiksnu.model.Activity)
(import 'jiksnu.model.User)

(def server (atom nil))
(def current-page (ref nil))
(def domain "localhost")
(def port 8085)
(def that-activity (ref nil))
(def that-user (ref nil))

(Before
   (c/load-config)
   (c/set-environment! :test)
   (dosync
    (reset! server (start port))))

(After
  (@server)
  (shutdown-agents))


(defn fetch-page
  [method path]
  (let [url (str "http://" domain ":" port path)
        request {:method method
                 :url url}
        response (sync-http-request request)]
    (dosync
     (ref-set current-page response))))

(defn a-user-exists
  []
  (with-database
    (let [user (actions.user/create (factory User))]
      (dosync
       (ref-set that-user user)))))


(Given #"the user is not logged in"
  (fn []))

(Given #"an? activity exists"
  (fn []
    (with-database
      (let [activity (actions.activity/create (factory Activity))]
        (dosync
         (ref-set that-activity activity))))))

(Given #"an? user exists" a-user-exists)

(Given #"a normal user is logged in"
  (fn []
    (a-user-exists)
    (session/set-authenticated-user! @that-user)))



(When #"I visit the home page"
  (fn []
    (fetch-page :get "/")))

(When #"I request the host-meta page"
  (fn []
    (fetch-page :get "/.well-known/host-meta")))

(When #"I request the subscription index page"
  (fn []
    (fetch-page :get "/admin/subscriptions")))

(When #"I request the user-meta page for that user"
  (fn []
    (fetch-page :get
                (str "/main/xrd?uri=" (model.user/get-uri @that-user)))))




(Then #"I should see an activity"
  (fn []
    (fact
      (-> @current-page :body channel-buffer->string) => #".*hentry")))

(Then #"I should see a list of activities"
  (fn []))

(Then #"the response is sucsessful"
  (fn []
    (fact
      (:status @current-page) => 200)))

(Then #"the response is a redirect"
  (fn []
    (fact
      (:status @current-page) => #(>= 300 %)
      (:status @current-page) => #(< 400 %))))

(Then #"the content-type is \"([^\"]+)\""
  (fn [type]
    (fact
      (get-in @current-page [:headers "Content-type"]) => type)))

(Then #"I am redirected to the login page"
  (fn []
    (fact
      (get @current-page [:headers "location"]) => #"/main/login")))
