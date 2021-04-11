(ns tiles.core
  (:require [tiles.ed :as ed]
            [tiles.io :as tio]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as jio]
            [clojure.string :refer [join] :rename {join str-join}]
            [clojure.spec.alpha :as s]))


(declare -loop)


(defmacro gracefully
  [& body]
  `(try
     ~@body
     (catch Exception e#
       (println (.getMessage e#)))))


(defn show
  [more path name]
  (gracefully
   (println (->> (tio/read-res path name)
                 (map #(str-join " " %))
                 (str-join "\n"))))
  [-loop more])


(defn *list
  [more path]
  (gracefully
   (println (->> (jio/file path)
                 .listFiles
                 (filter #(.isFile %))
                 (map #(.getName %))
                 (str-join "\n"))))
  [-loop more])


(defn delete
  [more path name]
  (gracefully
   (jio/delete-file (jio/file path name) true))
  [-loop more])


(defn create
  [more path name w h]
  (gracefully
   (tio/spit-res path name
                 (->> 0
                      (repeat w)
                      (apply vector)
                      (repeat h)
                      (apply vector))))
  [-loop more])


(defn open
  [more path name]
  (let [data (tio/read-res path name)]
    [ed/-loop [-loop path name data] []]))


(defn exit
  [more]
  [nil more])


(defn help
  [more]
  (pprint (let [[_ & pairs] (s/describe ::command)]
            (for [[k sk] (partition 2 pairs)]
              [k (s/describe sk)])))
  [-loop more])


(s/def ::create (s/tuple #{:create} string? string? int? int?))

(s/def ::list   (s/tuple #{:list} string?))

(s/def ::delete (s/tuple #{:delete} string? string?))

(s/def ::show   (s/tuple #{:show} string? string?))

(s/def ::open   (s/tuple #{:open} string? string?))

(s/def ::exit   (s/tuple #{:exit}))

(s/def ::help   (s/tuple #{:help}))

(s/def ::command    (s/or :create ::create
                          :list   ::list
                          :delete ::delete
                          :show   ::show
                          :open   ::open
                          :exit   ::exit
                          :help   ::help))


(def commands
  {:create create
   :list   *list
   :delete delete
   :show   show
   :open   open
   :exit   exit
   :help   help})


(defn -loop
  [cmd & more]
  (if (s/valid? ::command cmd)
    (let [[kwd & params] cmd]
      (apply (commands kwd) more params))
    (do
      (s/explain ::command cmd)
      (flush)
      [-loop more])))