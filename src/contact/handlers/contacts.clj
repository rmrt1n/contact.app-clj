(ns contact.handlers.contacts
  (:require [ring.util.response :as res]
            [hiccup2.core :as h]
            [contact.templates :as templates]
            [contact.handlers.archive :as archive]))

(defn toolbar-fragment []
  (h/html
   [:template {:x-if "selected.length > 0"}
    [:div.card.flex
     [:slot {:x-text "selected.length"}]
     " contacts selected"
     [:button {(keyword "x-on:click")
               (str "confirm(`delete ${selected.length} contacts?`) &&"
                    "htmx.ajax('DELETE', 
                               '/contacts', 
                               { source: $root, target: document.body})")}
      "delete"]
     [:button {(keyword "x-on:click") "selected = []"}
      "cancel"]]]))

(defn noscript-forms-fragment [contacts]
  (->> contacts
       (map
        (fn [c]
          (let [{:keys [id]} c]
            [:form {:id (str "delete-" id)
                    :method "post"
                    :action (str "/contacts/" id "/delete")}])))
       h/html))

(defn rows-fragment [contacts]
  (->> contacts
       (map
        (fn [c]
          (let [{:keys [id first last phone email]} c]
            [:tr
             [:td
              [:input {:x-model "selected"
                       :name "selected"
                       :type "checkbox"
                       :value id}]]
             [:td first] [:td last] [:td phone] [:td email]
             [:td
              [:div {:data-overflow-menu true}
               [:button {:type "button"
                         :aria-haspopup "menu"
                         :aria-controls (str "contact-menu-" id)}
                "options"]
               [:div {:id (str "contact-menu-" id)
                      :role "menu"
                      :hidden true
                      :style {:position "relative"}}
                [:div.card.flex.flex-col
                 {:style {:position "absolute"
                          :z-index 10
                          :left "50%"
                          :align-items "start"
                          :background "var(--bg)"
                          :border-radius "var(--border-radius)"}}
                 [:a {:role "menu-item" :href (str "/contacts/" id "/edit")}
                  "edit"]
                 [:a {:role "menu-item " :href (str "/contacts/" id)}
                  "view"]
                 [:button {:hx-delete (str "/contacts/" id)
                           :hx-target "closest tr"
                           :hx-swap "outerHTML swap:.5s"
                           :hx-confirm "are you sure you want to delete this contact?"
                           :id (str "hx-delete-" id)
                           :role "menu-item"
                           :form (str "delete-" id)} "delete"]]]]]])))

       h/html))

(defn contacts-page [q contacts page]
  (templates/layout
   [:form.space-y-2 {:method "get"
                     :action "/contacts"
                     :style {:max-width "24rem"}}
    [:label {:for "search"} "search term"]
    [:div.flex
     [:input#search {:hx-get "/contacts"
                     :hx-trigger "search, keyup delay:200ms changed"
                     :hx-target "tbody"
                     :hx-push-url "true"
                     :hx-indicator "#spin"
                     :_ "on keydown from the window me.focus()"
                     :name "q"
                     :type "search"
                     :value q}]
     [:div#spin.htmx-indicator.spinner]]
    [:button "search"]]
   (archive/archive-fragment nil 0)
   [:form.space-y-2 {:x-data "{selected: []}"
                     :method "post"
                     :action "/contacts"}
    [:table
     [:thead
      [:tr [:th] [:th "first"] [:th "last"] [:th "phone"] [:th "email"] [:th]]]
     [:tbody
      (rows-fragment contacts)]]
    [:div.flex
     (when (> page 1)
       [:a.btn {:href (str "?page=" (dec page)
                           (when (not (nil? q))
                             (str "&?q=" q)))} "prev"])
     (when (= 10 (count contacts))
       [:a.btn {:href (str "?page=" (inc page)
                           (when (not (nil? q))
                             (str "&?q=" q)))} "next"])

     (toolbar-fragment)]]
   [:p {:hx-get "/contacts/count" :hx-trigger "revealed"}
    [:span.htmx-indicator.spinner]]
   [:p
    [:a {:href "/contacts/new"} "add contact"]]
   [:noscript (noscript-forms-fragment contacts)]))

(defn contacts-search [contacts text]
  (let [re (re-pattern (str "(?i)" text))]
    (filterv (fn [c] (or (not (nil? (re-find re (str (:id c)))))
                         (not (nil? (re-find re (:first c))))
                         (not (nil? (re-find re (:last c))))
                         (not (nil? (re-find re (:phone c))))
                         (not (nil? (re-find re (:email c))))))
             contacts)))

(defn contacts-handler [{:keys [db query-params headers]}]
  (let [q          (get query-params "q")
        page       (-> (or (get query-params "page") "1") Integer/parseInt dec)
        hx-trigger (get headers "hx-trigger")
        filtered   (if (nil? q)
                     @db
                     (contacts-search @db q))
        contacts   (cond
                     (empty? filtered) []
                     (>= (+ 10 page) (count filtered)) (subvec filtered page)
                     :else (subvec filtered page (+ 10 page)))]
    (if (= "search" hx-trigger)
      (res/response (str (rows-fragment contacts)))
      (res/response (str (contacts-page q contacts (inc page)))))))

(defn contacts-count-handler [{:keys [db]}]
  (let [n (count @db)]
    ;; to test lazy loading
    (Thread/sleep 2000)
    (res/response
     (str "(" n " total contacts)"))))

(defn contacts-delete-handler [{:keys [db form-params]}]
  (let [selected  (or (get form-params "selected") [])
        ;; if selected is only 1 value, it is a string instead of a vector
        ;; so we have to convert it to vector manually smh
        selectedv (if (vector? selected)
                    selected
                    (vector selected))
        ids       (mapv #(Integer/parseInt %) selectedv)]
    (swap! db (fn [db]
                (filterv #(not (some #{(:id %)} ids)) db)))
    (res/response (str (contacts-page nil @db 0)))))
