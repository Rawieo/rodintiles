(ns tiles.io
  (:require [clojure.edn :as edn]
            [clojure.java.io :as jio]
            [clojure.pprint :refer [pprint]]))


(defn read-res
  [path name]
  (with-open [reader (jio/reader (jio/file path name))]
    (edn/read (java.io.PushbackReader. reader))))


(defn spit-res
  [path name res]
  (spit (jio/file path name) (with-out-str (pprint res))))
