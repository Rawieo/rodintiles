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


(defn run
  [f env]
  (when f
    (let [cmd (read-command (read-line))]
      (if cmd
        (let [[f1 env ps] (f cmd env)]
          (doseq [p! ps] (p!))
          (flush)
          (recur f1 env))
        (do
          (println "invalid command syntax")
          (recur f env))))))


(defn -main
  [& args]
  (run tcore/-loop {}))
