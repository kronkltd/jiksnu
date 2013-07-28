(ns jiksnu.routes.webfinger-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]))

(definitializer
  (require-namespaces
   ["jiksnu.filters.webfinger-filters"
    "jiksnu.views.webfinger-views"]))
