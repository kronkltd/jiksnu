(ns jiksnu.xmpp.user-repository
  (:require [jiksnu.model.user :as user])
  (:import tigase.xmpp.BareJID))

(defn -addDataList
  "addDataList method adds mode entries to existing data list associated with
given key in repository under given node path."
  [this #^BareJID user #^String subnodes list]
  ;; TODO: implement
  )

(defn -addUser
  "This addUser method allows to add new user to repository."
  [#^BareJID user]
  ;; TODO: implement
  (user/create user))

(defn #^String -getData
  "returns a value associated with given key for user repository in default
subnode."
  ([#^BareJID user
    #^String key]
     ;; TODO: implement
     )
  ([#^BareJID user #^String subnode #^String key]
     ;; TODO: implement
     )
  ([#^BareJID user
    #^String subnode
    #^String key
    #^String def]
     ;; TODO: implement
     ))

(defn -getDataList
  "returns array of values associated with given key or null if given key does
not exist for given user ID in given node path."
  [#^BareJID user #^String subnode #^String key]
  ;; TODO: implement
  )


(defn -getKeys
  "returns list of all keys stored in given subnode in user repository."
  ([#^BareJID user]
     ;; TODO: implement
     )
  ([#^BareJID user
    #^String subnode]
     ;; TODO: implement
     ))

(defn #^String -getResourceUri
  "Returns a DB connection string or DB connection URI."
  []
  ;; TODO: implement
  )

(defn -getSubnodes
  "returns list of all direct subnodes from given node."
  ([#^BareJID user]
     ;; TODO: implement
     )
  ([#^BareJID user
    #^String subnode]
     ;; TODO: implement
     ))

(defn -getUsers
  "This method is only used by the data conversion tools."
  []
  ;; TODO: implement
  (user/index))

(defn #^long -getUsersCount
  "This method is only used by the server statistics component to report number
of registered users"
  []
  ;; TODO: implement
  (count (user/index)))

(defn #^long -getUsersCount
  "This method is only used by the server statistics component to report number
of registered users"
  [#^String domain]
  ;; TODO: implement
  
  (user/index))

(defn #^long -getUsersUID
  "Returns a user unique ID number within the given repository."
  [#^BareJID user]
  ;; TODO: implement
  )

(defn -initRepository
  "The method is called to initialize the data repository."
  [#^String resource-uri params]
  ;; TODO: implement
  )

(defn -removeData
  "removes pair (key, value) from user repository in given subnode."
  ([#^BareJID user
    #^String key]
     ;; TODO: implement
     )
  ([#^BareJID user
    #^String subnode
    #^String key]
     ;; TODO: implement
     ))

(defn -removeSubnode
  "removes given subnode with all subnodes in this node and all data stored in
this node and in all subnodes."
  [#^BareJID user
   #^String subnode]
  ;; TODO: implement
  )

(defn -removeUser
  "allows to remove user and all his data from user repository."
  [#^BareJID user]
  ;; TODO: implement
  )

(declare get-id)
(declare get-domain)
(declare set-key)

(defn -setData
  "sets data value for given user ID in repository under given node path and
associates it with given key."
  ([#^BareJID user
    #^String key
    #^String value]
     ;; TODO: implement
     (let [id (get-id user)
           domain (get-domain user)]
       (set-key user key value)))
  ([#^BareJID user
    #^String subnode
    #^String key
    #^String value]
     ;; TODO: implement
     ))


(defn -setDataList
  "sets list of values for given user associated given key in repository under
given node path."
  [#^BareJID user
   #^String subnode
   #^String key
   list]
  ;; TODO: implement
  )

(defn -userExists
  "checks whether the user (or repository top node) exists in the database."
  [#^BareJID user])
