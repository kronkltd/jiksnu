(ns jiksnu.app
  (:require [jiksnu.app.loader :as loader]))

(defonce models  (atom {}))
(defonce jiksnu (loader/initialize-module!))
