(ns status-im.ui.screens.wallet.account.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.accordion :as accordion]
            [quo.core :as quo]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.wallet.account.styles :as styles]
            [status-im.ui.screens.wallet.accounts.sheets :as sheets]
            [status-im.ui.screens.wallet.accounts.views :as accounts]
            [status-im.ui.screens.wallet.buy-crypto.views :as buy-crypto]
            [status-im.ui.screens.wallet.transactions.views :as history]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.ui.components.tabs :as tabs]
            [quo.react-native :as rn]
            [status-im.utils.handlers :refer [<sub]]
            [quo.design-system.colors :as quo-colors]
            [status-im.utils.utils :as utils.utils])
  (:require-macros [status-im.utils.views :as views]))

(def state (reagent/atom {:tab :assets}))

(defn button [label icon color handler]
  [react/touchable-highlight {:on-press handler :style {:flex 1}}
   [react/view {:flex 1 :align-items :center :justify-content :center}
    [react/view {:flex-direction :row :align-items :center}
     [icons/icon icon {:color color}]
     [react/text {:style {:margin-left 8 :color color}} label]]]])

(def button-group-height 52)

(views/defview account-card [{:keys [address color type] :as account}]
  (views/letsubs [currency        [:wallet/currency]
                  portfolio-value [:account-portfolio-value address]
                  window-width    [:dimensions/window-width]
                  prices-loading? [:prices-loading?]]
    [react/view {:style (styles/card window-width color)}
     [react/view {:padding 16 :padding-bottom 12 :flex 1 :justify-content :space-between}
      [react/view {:style {:flex-direction :row}}
       (if prices-loading?
         [react/small-loading-indicator :colors/white-persist]
         [react/text {:style {:font-size 32 :color colors/white-persist :font-weight "600"}} portfolio-value])
       [react/text {:style {:font-size 32 :color colors/white-transparent-persist :font-weight "600"}} (str " " (:code currency))]]
      [quo/text {:number-of-lines 1
                 :ellipsize-mode  :middle
                 :monospace       true
                 :size            :small
                 :style           {:width       (/ window-width 3)
                                   :line-height 22
                                   :color       colors/white-transparent-70-persist}}
       (ethereum/normalized-hex address)]]
     [react/view {:position :absolute :top 12 :right 12}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:wallet/share-popover address])}
       [icons/icon :main-icons/share {:color                      colors/white-persist
                                      :accessibility-label :share-wallet-address-icon}]]]
     [react/view {:height                     button-group-height :background-color          colors/black-transparent-20
                  :border-bottom-right-radius 8                   :border-bottom-left-radius 8 :flex-direction :row}
      (if (= type :watch)
        [react/view {:flex 1 :align-items :center :justify-content :center}
         [react/text {:style {:margin-left 8 :color colors/white-persist}}
          (i18n/label :t/watch-only)]]
        [button
         (i18n/label :t/wallet-send)
         :main-icons/send
         colors/white-persist
         #(re-frame/dispatch [:wallet/prepare-transaction-from-wallet account])])
      [react/view {:style (styles/divider)}]
      [button
       (i18n/label :t/receive)
       :main-icons/receive
       colors/white-persist
       #(re-frame/dispatch [:wallet/share-popover address])]]]))

(views/defview transactions [address]
  (views/letsubs [data [:wallet.transactions.history/screen address]]
    [history/history-list data address]))

(defn nft-assets-skeleton [num-assets]
  [:<>
   (for [i (range num-assets)]
     ^{:key i}
     [react/view {:style {:width         "48%"
                          :margin-bottom 16}}
      [react/view {:style  {:flex          1
                            :aspect-ratio  1
                            :border-width  1
                            :background-color "#EEEEEE"
                            :border-color  "#EEF2F5"
                            :border-radius 16}}]])])

(defn nft-assets [{:keys [num-assets address collectible-slug]}]
  (let [assets (<sub [:wallet/opensea-assets-by-collection-and-address address collectible-slug])]
    [react/view {:flex            1
                 :flex-wrap       :wrap
                 :justify-content :space-between
                 :flex-direction  :row
                 :style           {:padding-horizontal 16}}
     (if (seq assets)
       (for [asset assets]
         ^{:key (:id asset)}
         [react/view {:style {:width         "48%"
                              :margin-bottom 16}}
          [react/image {:style  {:flex          1
                                 :aspect-ratio  1
                                 :border-width  1
                                 :border-color  "#EEF2F5"
                                 :border-radius 16}
                        :source {:uri (:image_url asset)}}]])
       [nft-assets-skeleton num-assets])]))

(defn nft-collections [address]
  (let [collection (<sub [:wallet/opensea-collection address])]
    [:<>
     (for [collectible collection]
       ^{:key (:slug collectible)}
       [accordion/section
        {:title
         [react/view {:flex 1}
          [quo/list-item
           {:title          (:name collectible)
            :text-size      :large
            :icon
            [list/item-image {:style  {:border-radius 40
                                       :overflow      :hidden
                                       :border-width  1
                                       :border-color  "#EEF2F5"}
                              :source {:uri (:image_url collectible)}}]
            :accessory      :text
            :accessory-text (:owned_asset_count collectible)}]]
         :padding-vertical     0
         :dropdown-margin-left -12
         :on-open              #(re-frame/dispatch [::wallet/get-opensea-assets-by-owner-and-collection
                                                    address
                                                    (:slug collectible)
                                                    (:owned_asset_count collectible)])
         :content              [nft-assets {:address          address
                                            :num-assets       (:owned_asset_count collectible)
                                            :collectible-slug (:slug collectible)}]}])]))

(views/defview assets-and-collections [address]
  (views/letsubs [{:keys [tokens]} [:wallet/visible-assets-with-values address]
                  currency [:wallet/currency]
                  opensea-collection [:wallet/opensea-collection address]]
    (let [{:keys [tab]} @state]
      [react/view {:flex 1}
       [react/view {:flex-direction :row :margin-bottom 8 :padding-horizontal 4}
        [tabs/tab-title state :assets (i18n/label :t/wallet-assets) (= tab :assets)]
        [tabs/tab-title state :nft (i18n/label :t/wallet-collectibles) (= tab :nft)]
        [tabs/tab-title state :history (i18n/label :t/history) (= tab :history)]]
       (cond
         (= tab :assets)
         [:<>
          [buy-crypto/banner]
          (for [item tokens]
            ^{:key (:name item)}
            [accounts/render-asset item nil nil (:code currency)])]
         (= tab :nft)
         [react/view
          (if (seq opensea-collection)
            [nft-collections address]
            [react/view {:align-items :center :margin-top 32}
             [react/text {:style {:color colors/gray}}
              (i18n/label :t/no-collectibles)]])]
         (= tab :history)
         [transactions address])])))

(views/defview bottom-send-recv-buttons [{:keys [address type] :as account} anim-y]
  [react/animated-view {:style {:background-color colors/white
                                :bottom           0
                                :flex-direction   :row
                                :height           button-group-height
                                :position         :absolute
                                :shadow-offset    {:width 0 :height 1}
                                :shadow-opacity   0.75
                                :shadow-radius    1
                                :transform        [{:translateY anim-y}]
                                :width            "100%"}}
   (if (= type :watch)
     [react/view {:flex 1 :align-items :center :justify-content :center}
      [react/text {:style {:margin-left 8 :color colors/blue-persist}}
       (i18n/label :t/watch-only)]]
     [button
      (i18n/label :t/wallet-send)
      :main-icons/send
      colors/blue-persist
      #(re-frame/dispatch [:wallet/prepare-transaction-from-wallet account])])
   [button
    (i18n/label :t/receive)
    :main-icons/receive
    colors/blue-persist
    #(re-frame/dispatch [:wallet/share-popover address])]])

(defn anim-listener [anim-y scroll-y]
  (let [to-show (atom false)]
    (animation/add-listener
     scroll-y
     (fn [anim]
       (let [y-trigger 149]
         (cond
           (and (>= (.-value anim) y-trigger) (not @to-show))
           (animation/start
            (styles/bottom-send-recv-buttons-raise anim-y)
            #(reset! to-show true))

           (and (< (.-value anim) y-trigger) @to-show)
           (animation/start
            (styles/bottom-send-recv-buttons-lower anim-y button-group-height)
            #(reset! to-show false))))))))

;; Note(rasom): sometimes `refreshing` might get stuck on iOS if action happened
;; too fast. By updating this atom in 1s we ensure that `refreshing?` property
;; is updated properly in this case.
(def updates-counter (reagent/atom 0))

(defn schedule-counter-reset []
  (utils.utils/set-timeout
   (fn []
     (swap! updates-counter inc)
     (when @(re-frame/subscribe [:wallet/refreshing-history?])
       (schedule-counter-reset)))
   1000))

(defn refresh-action []
  (schedule-counter-reset)
  (re-frame/dispatch [:wallet.ui/pull-to-refresh-history]))

(defn refresh-control [refreshing?]
  (reagent/as-element
   [rn/refresh-control
    {:refreshing (boolean refreshing?)
     :onRefresh  refresh-action}]))

(views/defview account []
  (views/letsubs [{:keys [name address] :as account} [:multiaccount/current-account]
                  fetching-error [:wallet/fetching-error]]
    (let [anim-y (animation/create-value button-group-height)
          scroll-y (animation/create-value 0)]
      (anim-listener anim-y scroll-y)
      [:<>
       [topbar/topbar
        {:title name
         :right-accessories
         [{:icon     :main-icons/more
           :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                          {:content        sheets/account-settings
                                           :content-height 60}])}]}]
       [react/animated-scroll-view
        {:contentContainerStyle {:padding-bottom button-group-height}
         :on-scroll             (animation/event
                                 [{:nativeEvent {:contentOffset {:y scroll-y}}}]
                                 {:useNativeDriver true})
         :scrollEventThrottle   1
         :refreshControl        (refresh-control
                                 (and
                                  @updates-counter
                                  @(re-frame/subscribe [:wallet/refreshing-history?])))}
        (when fetching-error
          [react/view {:style {:flex 1
                               :align-items :center
                               :margin 8}}
           [icons/icon
            :main-icons/warning
            {:color           :red
             :container-style {:background-color (quo-colors/get-color :negative-02)
                               :height           40
                               :width            40
                               :border-radius    20
                               :align-items      :center
                               :justify-content  :center}}]
           [react/view
            {:style {:justify-content   :center
                     :align-items       :center
                     :margin-top        8
                     :margin-horizontal 67.5
                     :text-align        :center}}
            [quo/text
             {:color :secondary
              :size  :small
              :style {:text-align :center}}
             (i18n/label :t/transfers-fetching-failure)]]])
        [react/view {:padding-left 16}
         [react/scroll-view {:horizontal true}
          [react/view {:flex-direction :row :padding-top 8 :padding-bottom 12}
           [account-card account]]]]
        [assets-and-collections address]]
       [bottom-send-recv-buttons account anim-y]])))
