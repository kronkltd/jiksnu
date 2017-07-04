(ns jiksnu.app.services-test
  (:require [jiksnu.app.services :as services]))

(declare $http)

(js/describe "jiksnu.app.services"
  (fn []

    (js/describe "pageService"
      (fn []

        (js/it "should"
          (fn []

            (let [page-name "activities"
                  service (services/pageService $http)]

              (-> (js/expect (.fetch service page-name))
                  (.toEqual 7)))))))))
