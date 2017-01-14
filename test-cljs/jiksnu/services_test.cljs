(ns jiksnu.services-test
  (:require [jiksnu.services :as services]))

(declare $http)

(js/describe "jiksnu.services"
  (fn []

    (js/describe "pageService"
      (fn []

        (js/it "should"
          (fn []

            (let [page-name "activities"
                  service (services/pageService $http)]

              (-> (js/expect (.fetch service page-name))
                  (.toEqual 7)))))))))
