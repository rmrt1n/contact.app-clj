(ns contact.models)

(def contacts
  [{:id    1
    :first "John"
    :last  "Smith"
    :phone "123456789"
    :email "john.smith@e.com"}
   {:id    2
    :first "Dana"
    :last  "Crandith"
    :phone "123456789"
    :email "data.crandith@e.com"}
   {:id    3
    :first "Edit"
    :last  "Neutvar"
    :phone "123456789"
    :email "edit.neutvar@e.com"}])

(defn get-contact-by-id [contacts contact-id]
  (first (filter #(= contact-id (:id %)) contacts)))

(defn valid-contact? [contacts contact]
  (empty? (filter #(= (:email contact) (:email %)) contacts)))
