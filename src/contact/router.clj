(ns contact.router
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as params]
            [ring.util.response :as res]
            [contact.handlers.contacts :as contacts]
            [contact.handlers.new :as new]
            [contact.handlers.show :as show]
            [contact.handlers.edit :as edit]
            [contact.handlers.delete :as delete]
            [contact.handlers.validate :as validate]
            [contact.handlers.archive :as archive]
            [contact.models :as models]))

(defonce db (atom models/contacts))
(defonce archiver (atom nil))
(defonce progress (atom 0))

#_(reset! db (into [] (flatten (repeat 10 models/contacts))))
#_(reset! db models/contacts)
#_(reset! progress 0)
#_(reset! archiver nil)

(defn index [_]
  (res/response "ok"))

(def middleware-db
  {:name ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req
                                  :db db
                                  :progress progress
                                  :archiver archiver)))))})

(def routes
  (ring/ring-handler
   (ring/router
    [["/" {:get (fn [_] (res/redirect "/contacts"))}]
     ["/contacts"
      ["" {:get    contacts/contacts-handler
           :post   contacts/contacts-delete-handler
           :delete contacts/contacts-delete-handler}]
      ["/new" {:get  new/new-handler
               :post new/post-new-handler}]
      ["/count" {:get contacts/contacts-count-handler}]
      ["/archive"
       ["" {:get archive/archive-handler
            :post archive/post-archive-handler
            :delete archive/archive-delete-handler}]
       ["/file" {:get archive/archive-file-handler}]]
      ["/:contact-id"
       ["" {:get    show/show-handler
            :delete delete/delete-handler}]
       ["/edit" {:get  edit/edit-handler
                 :post edit/post-edit-handler}]
       ["/delete" {:post delete/delete-handler}]
       ["/email" {:get validate/email-handler}]]]]
    {:data {:db db
            :archiver archiver
            :progress progress
            :middleware [params/parameters-middleware
                         middleware-db]}
     :conflicts nil})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler
     {:not-found (fn [_] {:status 404 :body "not found"})}))))
