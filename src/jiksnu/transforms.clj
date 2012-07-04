(ns jiksnu.transforms
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]))

(defn set-_id
  [record]
  (if (:_id record)
    record
    (assoc record :_id (model/make-id))))

(defn set-created-time
  [record]
  (if (:created record)
    record
    (assoc record :created (time/now))))

(defn set-updated-time
  [record]
  (if (:updated record)
    record
    (assoc record :updated (time/now))))

