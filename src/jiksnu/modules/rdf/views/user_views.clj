(ns jiksnu.modules.rdf.views.user-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [show-section]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        jiksnu.actions.user-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to format-page-info pagination-links with-page]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
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

