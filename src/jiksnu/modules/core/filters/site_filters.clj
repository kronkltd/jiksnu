(ns jiksnu.modules.core.filters.site-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.session :only [current-user-id]]
        jiksnu.actions.site-actions))

(deffilter #'rsd :http
  [action request]
  (action))

(deffilter #'service :http
  [action request]
  (action (current-user-id)))

(deffilter #'get-stats :command
  [action request]
  (action))

(deffilter #'get-stats :http
  [action request]
  (action))

(deffilter #'ping :command
  [action request]
  (apply action (:args request)))

;; (deffilter #'get-load :command
;;   [action request]
;;   (apply action (:args request)))

(deffilter #'get-environment :command
  [action request]
  (apply action (:args request)))

(deffilter #'get-config :command
  [action request]
  (apply action (:args request)))

