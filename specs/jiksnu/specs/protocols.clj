(ns jiksnu.specs.protocols
    (:require [clojure.test :refer :all]))

(defprotocol Page
  (load-page [this]))

(defprotocol LoginPageProto
  (login [this username password])
  (set-password [this password])
  (set-username [this username])
  (submit [this]))

