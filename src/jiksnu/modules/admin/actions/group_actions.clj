(ns jiksnu.modules.admin.actions.group-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]))

(defaction index
  [& options]
  (apply actions.group/index options))

(defaction create
  [params]
  (actions.group/create params))

(defaction delete
  [group]
  (actions.group/delete group))

(defaction show
  [group]
  (actions.group/show group))
