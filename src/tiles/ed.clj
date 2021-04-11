(ns tiles.ed
  (:require [tiles.io :as tio]
            [clojure.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]))


(declare -loop)


(defn read*
  [back path name data x y]
  (pprint (get-in data [x y]))
  [-loop [back path name data]])


(defn write
  [back path name data x y z]
  [-loop [back path name (assoc-in data [x y] z)]])


(defn overwrite
  [back path name data z]
  [-loop [back path name (mapv #(mapv (constantly z) %) data)]])


(defn close
  [back path name data]
  (tio/spit-res path name data)
  [back []])


(defn help
  [back path name data]
  (pprint
   (let [[_ & pairs] (s/describe ::command)]
     (for [[k sk] (partition 2 pairs)]
       [k (s/describe sk)])))
  [back [path name data]])


(s/def ::write-cmd  (s/tuple #{:write} int? int? int?))

(s/def ::read-cmd   (s/tuple #{:read} int? int?))

(s/def ::fill-cmd   (s/tuple #{:fill} int?))

(s/def ::close-cmd  (s/tuple #{:close}))

(s/def ::help-cmd   (s/tuple #{:help}))

(s/def ::command    (s/or :write  ::write-cmd
                          :read   ::read-cmd
                          :fill   ::fill-cmd
                          :close  ::close-cmd
                          :help   ::help-cmd))


(def commands
  {:read      read*
   :write     write
   :close     close
   :overwrite overwrite
   :help      help})


(defn -loop
  [cmd back path name data]
  (if (s/valid? ::command cmd)
    (let [[kwd & params] cmd]
      (apply (commands kwd) back path name data params))
    (do
      (s/explain ::command cmd)
      (flush)
      [-loop [back path name data]])))
