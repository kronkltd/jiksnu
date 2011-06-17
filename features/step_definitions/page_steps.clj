(use 'jiksnu.http)
(use 'jiksnu.model)
(use 'clj-factory.core)
(import 'jiksnu.model.Activity)
(require 'jiksnu.model.activity)
(require 'ciste.config)

(def server (atom nil))

(Before
 (alter-var-root #'ciste.config/*environment* :test)
 (dosync
  (ref-set jiksnu.model/*mongo-database* (mongo-database))
  (reset! server (with-environment :test (start 8085)))))

(After
 (println "Going down")
 (println server)
 (@server)
 )

(Given #"the user is not logged in"
      #(identity nil)

       )

(Given #"an? activity exists"
       (fn []
         (with-environment :test
          (jiksnu.model.activity/create (factory Activity)))))

(When #"I visit the home page"
      #(identity nil)
      )

(Then #"I should see an activity"
      #(println "seeing an activity"))

(Then #"I should see a list of activities"
      #(identity nil)
      )
