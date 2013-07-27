(ns jiksnu.actions.admin.feed-subscription-actions
  (:use [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]
            [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-subscription))

(defaction index
  [& options]
  (apply index* options))

(defaction delete
  [record]
  (cm/implement))

(defaction show
  [record]
  (cm/implement))
