(ns contact.handlers.archive
  (:require [ring.util.response :as res]
            [hiccup2.core :as h]))

(defn archive-fragment [archiver progress]
  (h/html
   [:div {:hx-target "this"
          :hx-swap "outerHTML"
          :id "archive-ui"}
    (cond
      (nil? archiver)
      [:button {:hx-post "/contacts/archive"} "download contact archive"]
      (future-done? @archiver)
      [:div.space-x-2
       [:a {:hx-boost "false" :href "/contacts/archive/file"}
        "archive ready! Click here to download. ↓"]
       [:button {:hx-delete "/contacts/archive"} "clear download"]]
      :else
      [:div.space-y-2 {:hx-get "/contacts/archive"
                       :hx-trigger "load delay:500ms"}
       [:p "creating archive..."]
       [:div.progress
        [:div#archive-progress.progress-bar
         {:role "progressbar"
          :aria-valuenow @progress
          :style {:width (str @progress "%")}}]]])]))

(defn archive-handler [{:keys [archiver progress]}]
  (res/response (str (archive-fragment archiver progress))))

(defn post-archive-handler [{:keys [archiver progress]}]
  (swap! archiver
         (constantly
          (future (loop []
                    (when (not= 100 @progress)
                      (println "progressing...")
                      (swap! progress #(+ 10 %))
                      (Thread/sleep 2000)
                      (recur))))))
  (res/response (str (archive-fragment archiver progress))))

(defn archive-delete-handler [{:keys [archiver progress]}]
  (reset! archiver nil)
  (reset! progress 0)
  (res/response (str (archive-fragment nil 0))))

(defn archive-file-handler [_]
  (-> "resources/public/data.edn"
      res/file-response
      (res/header "Content-Disposition" "attachment;filename=data.edn")))
