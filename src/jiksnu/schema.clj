(ns jiksnu.schema
  (:use [clj-schema.schema :only [def-loose-schema defschema optional-path
                                  sequence-of set-of]])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(defschema dated-schema
  [[:created]  DateTime
   [:updated]  DateTime])

(defschema domain-schema
  dated-schema
  [
   [:_id]                                                       String
   [:discovered]                                                Boolean
   [:local]                                                     Boolean
   (optional-path [:statusnet-config :attachments :file_quota]) Long
   (optional-path [:statusnet-config :group       :desclimit])  String
   (optional-path [:statusnet-config :license     :type])       String
   (optional-path [:statusnet-config :license     :url])        String
   (optional-path [:statusnet-config :site        :closed])     Boolean
   (optional-path [:statusnet-config :site        :email])      String
   (optional-path [:statusnet-config :site        :fancy])      Boolean
   (optional-path [:statusnet-config :site        :logo])       String
   (optional-path [:statusnet-config :site        :name])       String
   (optional-path [:statusnet-config :site        :private])    String
   (optional-path [:statusnet-config :site        :server])     String
   (optional-path [:statusnet-config :site        :ssl])        String
   (optional-path [:statusnet-config :xmpp        :enabled])    Boolean
   (optional-path [:statusnet-config :xmpp        :server])     String
   (optional-path [:statusnet-config :xmpp        :user])       String
   ])
