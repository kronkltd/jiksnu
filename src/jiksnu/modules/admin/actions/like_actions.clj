(ns jiksnu.modules.admin.actions.like-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :as actions.like]))

(defaction index
  [& options]
  (apply actions.like/index options))

(defaction delete
  [& options]
  (apply actions.like/delete options))
