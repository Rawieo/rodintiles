(ns tiles.ed
  (:require [tiles.io :as tio]
            [clojure.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]))


(defn read*
  [_ -loop env tiles x y]
  (pprint [env tiles x y])
  [-loop env tiles [#(pprint (get-in tiles [x y]))]])


(defn write
  [_ -loop env tiles x y z]
  [-loop env (assoc-in tiles [x y] z) []])


(defn overwrite
  [_ -loop env tiles z]
  [-loop env (mapv #(mapv (constantly z) %) tiles) []])


(defn close
  [bck _ env tiles]
  (let [{:keys [path name]} env]
    [bck (dissoc env :path :name) tiles [#(tio/spit-res path name tiles)]]))


(defn help
  [_ -loop env]
  [-loop env [#(pprint (let [[_ & pairs] (s/describe ::command)]
                         (for [[k sk] (partition 2 pairs)]
                           [k (s/describe sk)])))]])


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
  [bck dat]
  (let [tiles (atom dat)]
    (fn -loop* [cmd env]
      (if (s/valid? ::command cmd)
        (let [[kwd & params] cmd
              [bck1 env1 tiles1 ps] (apply (commands kwd) bck -loop* env @tiles params)]
          (reset! tiles tiles1)
          [bck1 env1 ps])
        [-loop* env [#(s/explain ::command cmd)]]))))
