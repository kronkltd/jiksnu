(ns jiksnu.wrappers.activity-wrappers)

(defwrapper #'index :http
  [f request]
  (f request))
