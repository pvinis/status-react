(ns status-im.ui.screens.chat.image.preview.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [quo.platform :as platform]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [quo.components.safe-area :as safe-area]
            ["react-native-image-viewing" :default image-viewing]))

(defn footer-options []
  (fn [{:keys [message on-close]}]
      ;; FIXME(Ferossgp): Bottom sheet doesn't work on Android because of https://github.com/software-mansion/react-native-gesture-handler/issues/139
    [react/view {:style {:flex-direction :row
                         :background-color colors/black-transparent-86
                         :border-radius    44
                         :padding          8
                         :position         :absolute
                         :bottom           0
                         :right            0}}
     [react/touchable-opacity
      {:on-press (fn []
                   (on-close)
                   (re-frame/dispatch [:chat.ui/save-image-to-gallery (get-in message [:content :image])]))
       :style    {:margin-right 10}}
      [icons/icon :main-icons/download {:container-style {:width  24
                                                          :height 24}
                                        :color           colors/white-persist}]]
     [react/touchable-opacity
      {:on-press (fn []
                   (on-close)
                   (re-frame/dispatch [:chat.ui/save-image-to-gallery (get-in message [:content :image])]))
       :style    {:margin-left 10}}
      [icons/icon :main-icons/share {:container-style {:width  24
                                                       :height 24}
                                     :color           colors/white-persist}]]]))

(defn header [{:keys [on-close] :as props}]
  [safe-area/consumer
   (fn [insets]
     [react/view {:style {:padding-horizontal 15
                          :padding-top     (+ (:bottom insets) 30)}}
      [react/view {:style {:justify-content :center}}
       [react/touchable-opacity {:on-press on-close
                                 :style    {:background-color   colors/black-transparent-86
                                            :padding-vertical   11
                                            :border-radius      44}}
        [icons/icon :main-icons/close {:container-style {:width  24
                                                         :height 24}
                                       :color           colors/white-persist}]]
       [footer-options props]]])])

(defn preview-image [{{:keys [content] :as message} :message
                      visible                       :visible
                      on-close                      :on-close}]
  [:> image-viewing {:images                 #js [#js {:uri (:image content)}]
                     :on-request-close       on-close
                     :hide-header-on-zoom    false
                     :hide-footer-on-zoom    false
                     :swipe-to-close-enabled platform/ios?
                     :presentation-style     "overFullScreen"
                     :HeaderComponent        #(reagent/as-element [header {:on-close on-close
                                                                           :message  message}]) ; NOTE: Keep it to remove default header
                     :visible                visible}])
