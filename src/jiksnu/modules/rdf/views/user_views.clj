(ns jiksnu.modules.rdf.views.user-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            jiksnu.actions.user-actions
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-page]]
            [jiksnu.namespace :as ns]
            [plaza.rdf.core :as plaza]
            [ring.util.response :as response]))

(defview #'show :n3
  [request user]
  {:body
   (let [rdf-model
         (plaza/defmodel (plaza/model-add-triples
                          (with-format :rdf
                            (show-section user))))]
     (with-out-str (plaza/model->format rdf-model :n3)))
   :template :false})

(defview #'show :rdf
  [request user]
  {:body
   (let [rdf-model (plaza/defmodel (plaza/model-add-triples (show-section user)))]
     (with-out-str (plaza/model->format rdf-model :xml-abbrev)))
   :template :false})

