(ns tiles.main
  (:require [tiles.core :as tcore]
            [clojure.edn :as edn]))


(defn read-command
  [line]
  (try
    (->> (str "[" line "]")
         edn/read-string
         (mapv #(if (symbol? %) (keyword %) %)))
    (catch Exception _ nil)))


(defn roam
  ([f r] (roam f r []))
  ([f r more]
   (loop [[f more] [f more]]
     (when f
       (recur (r f more))))))


(defn roamer
  [f more]
  (if-let [cmd (read-command (read-line))]
    (apply f cmd more)
    (do
      (println "invalid command syntax")
      [f more])))


(defn -main
  [& args]
  (roam tcore/-loop roamer))
