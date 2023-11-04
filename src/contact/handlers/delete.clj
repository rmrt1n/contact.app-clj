(ns contact.handlers.delete
  (:require [ring.util.response :as res]))

(defn delete-handler [{:keys [db path-params headers]}]
  (let [contact-id (-> path-params
                       (get :contact-id)
                       Integer/parseInt)
        hx-trigger (get headers "hx-trigger")]
    (swap! db (fn [db] (filterv #(not= contact-id (:id %)) db)))
    (if (and (not (nil? hx-trigger))
             (re-find #"hx-delete" hx-trigger))
      (res/response "")
      (res/redirect "/contacts" 303))))
