(ns jiksnu.step-macros)

(defmacro step-definitions
  [& body]
  (list 'set! (list '.-exports 'js/module)
        (apply list 'fn [] body)))



(defmacro Given
  [pattern bind & body]
  (list 'this-as 'this
        (list '.Given 'this pattern
              (concat
               (apply list 'fn bind body)
               (list nil)))))

(defmacro When
  [pattern bind & body]
  (list 'this-as 'this
        (list '.When 'this pattern
              (concat
               (apply list 'fn bind body)
               (list nil)))))

(defmacro Then
  [pattern bind & body]
  (list 'this-as 'this
        (list '.Then 'this pattern
              (concat
               (apply list 'fn bind body)
               (list nil)))))

(defmacro And
  [pattern bind & body]
  (list 'this-as 'this
        (list '.And 'this pattern
              (concat
               (apply list 'fn bind body)
               (list nil)))))
