(ns jiksnu.commands
  (:use  [ciste.commands :only [add-command!]]
         [ciste.core :only [serialize-as]]
         [ciste.filters :only [deffilter]]
         [ciste.views :only [defview]])
  (:require [ciste.workers :as workers]
            [clojure.string :as string]))

(defn ping
  []
  "pong")

(deffilter #'ping :command
  [action request]
  (apply action (:args request)))

(defview #'ping :text
  [request data]
  data)

(add-command! "ping" #'ping)





;; (defn get-load
;;   []
;;   (str (core.host/get-load-average)))

;; (deffilter #'get-load :command
;;   [action request]
;;   (apply action (:args request)))

;; (defview #'get-load :text
;;   [request data]
;;   data)

;; (add-command! "get-load" #'get-load)





(defn get-environment
  []
  (ciste.config/environment))

(deffilter #'get-environment :command
  [action request]
  (apply action (:args request)))

(defview #'get-environment :text
  [request data]
  data)

(add-command! "get-environment" #'get-environment)





(defn get-config
  [path]
  (->> (string/split path #"/")
       (map keyword)
       (apply ciste.config/config)))

(deffilter #'get-config :command
  [action request]
  (apply action (:args request)))

(defview #'get-config :text
  [request data]
  data)

(add-command! "config" #'get-config)





;; TODO: Pass back the worker

(defn start-worker
  [& args]
  (:id (workers/start-worker! (keyword (first args)))))

(deffilter #'start-worker :command
  [action request]
  (apply action (:args request)))

(defview #'start-worker :text
  [request data]
  data)

(add-command! "start-worker" #'start-worker)



(defn command-not-found
  []
  "Command not found")


(defmethod serialize-as :command
  [serialization response]
  response)
