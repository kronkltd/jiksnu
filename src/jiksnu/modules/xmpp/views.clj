(ns jiksnu.views
  (:use [ciste.core :only [serialize-as with-format]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.views :only [defview]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri title link-to
                                       index-block index-section uri
                                       delete-button index-line edit-button]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions :as actions]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            jiksnu.sections
            [plaza.rdf.core :as rdf]
            [plaza.rdf.vocabularies.foaf :as foaf]))

(defmethod serialize-as :xmpp
  [serialization response]
  (when response
    (tigase/make-packet response)))

