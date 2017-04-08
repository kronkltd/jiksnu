(ns jiksnu.routes.helpers)

(defn named-path
  [& _]
  "")

(defn add-route! [& _]
  nil)

(defn formatted-path
  ([name]
   (str (named-path name) ".:format"))
  ([name params]
   (str (named-path name params) ".:format"))
  ([name params format]
   (str (named-path name params) "." format)))

(defn formatted-url
  ([name]
   (str #_(named-url name) ".:format"))
  ([name params]
   (str #_(named-url name params) ".:format"))
  ([name params format]
   (str #_(named-url name params) "." format)))
