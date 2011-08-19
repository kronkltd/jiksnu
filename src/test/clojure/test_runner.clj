(use 'clojure.test)
(use 'clojure.test.junit)

(def escape-xml-map
  (zipmap "'<>\"&" (map #(str \& % \;) '[apos lt gt quot amp])))

(defn- escape-xml [text]
  (apply str (map #(escape-xml-map % %) text)))

(defn xml-escaping-writer
  [writer]
  (proxy
    [java.io.FilterWriter] [writer]
    (write [text]
      (if (string? text)
        (.write writer (escape-xml text))
        (.write writer text)))
    ))

(when-not *compile-files*
  (let [results (atom [])]
    (let [report-orig report
          junit-report-orig junit-report]
      (with-junit-output
        (run-all-tests)))
    (shutdown-agents)
    (System/exit (if (empty? (filter #{:fail :error} @results)) 0 -1))))
