(ns jiksnu.modules.web.core
  (:require [jiksnu.modules.http.resources :refer [defsite]]
            [octohipster.documenters.schema :refer [schema-doc schema-root-doc]]
            [octohipster.documenters.swagger :refer [swagger-doc]]))

(defsite jiksnu
  :description "Jiksnu Social Networking"
  :schemes ["http"]
  :documenters [swagger-doc schema-doc schema-root-doc])
