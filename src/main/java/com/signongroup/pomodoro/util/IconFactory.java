package com.signongroup.pomodoro.util;

import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Utility factory for FontIcons to centralize icon literals, enforce default sizes, and provide
 * programmatic builder methods to reduce boilerplate.
 */
public final class IconFactory {

  // Timer Controls
  public static final String PLAY = "fltfal-play-24";
  public static final String PAUSE = "fltfal-pause-24";
  public static final String STOP = "fltfal-stop-24";
  public static final String PLAY_20 = "fltfmz-play-20";
  public static final String STOP_20 = "fltfmz-stop-20";
  public static final String TIMER_20_FILLED = "fltfmz-timer-20";
  public static final String TIMER_20_REGULAR = "fltrmz-timer-20";

  // Navigation/Global
  public static final String SETTINGS = "fltfal-settings-24";
  public static final String BOARD = "fltfal-board-24";
  public static final String MENU = "fltral-line-horizontal-3-20";

  // Jira Issue Types
  public static final String TASK_BUG = "fltfal-bug-24";
  public static final String TASK_STORY = "fltfal-book-24";
  public static final String TASK_EPIC = "fltfal-flash-24";

  // Jira Priorities
  public static final String PRIO_HIGH = "fltfal-arrow-up-24";
  public static final String PRIO_LOW = "fltfal-arrow-down-24";

  // App Navigation / Window Controls
  public static final String MINIMIZE = "fltfal-arrow-minimize-20";
  public static final String MAXIMIZE = "fltfmz-maximize-20";

  // UI Elements and Actions
  public static final String ADD = "fltfal-add-20";
  public static final String SYNC = "fltfal-arrow-sync-20";
  public static final String CHECKMARK_SQUARE = "fltfal-checkmark-square-24";
  public static final String CHECKMARK_SQUARE_REGULAR = "fltral-checkmark-square-24";
  public static final String CLOCK = "fltfal-clock-24";
  public static final String DISMISS = "fltfal-dismiss-24";
  public static final String EYE_SHOW = "fltfal-eye-show-20";
  public static final String FILTER = "fltfal-filter-20";
  public static final String FOLDER = "fltfal-folder-24";
  public static final String LAYER = "fltfal-layer-20";
  public static final String LINE_HORIZONTAL = "fltfal-line-horizontal-1-20";

  public static final String ARROW_CLOCKWISE = "fltral-arrow-clockwise-20";
  public static final String CHECKMARK_CIRCLE = "fltral-checkmark-circle-20";
  public static final String CHEVRON_DOWN = "fltral-chevron-down-20";
  public static final String CHEVRON_LEFT = "fltral-chevron-left-20";
  public static final String CHEVRON_RIGHT = "fltral-chevron-right-24";
  public static final String DRINK_COFFEE = "fltral-drink-coffee-20";
  public static final String FOOD = "fltral-food-20";
  public static final String GRID = "fltral-grid-24";

  public static final String NEXT = "fltrmz-next-24";

  public static final String TASK_LIST = "fltfmz-task-list-24";
  public static final String ARROW_SWAP = "fltral-arrow-swap-20";
  public static final String PERSON = "fltrmz-person-20";

  /** Prevent instantiation. */
  private IconFactory() {}

  /**
   * Instantiates a new FontIcon with the specified size and appends "app-icon" to its style
   * classes.
   *
   * @param iconLiteral the FluentUI icon literal
   * @param size the size of the icon
   * @return the newly created FontIcon
   */
  public static FontIcon create(final String iconLiteral, final int size) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(size);
    icon.getStyleClass().add("app-icon");
    return icon;
  }

  /**
   * Instantiates a new FontIcon defaulting to 16px size and appends "app-icon" to its style
   * classes.
   *
   * @param iconLiteral the FluentUI icon literal
   * @return the newly created FontIcon
   */
  public static FontIcon create(final String iconLiteral) {
    return create(iconLiteral, 16);
  }

  /**
   * Instantiates a new FontIcon, sets its size, adds "app-icon", and appends a custom CSS class.
   *
   * @param iconLiteral the FluentUI icon literal
   * @param size the size of the icon
   * @param customCssClass the additional CSS class to apply
   * @return the newly created FontIcon
   */
  public static FontIcon createWithClass(
      final String iconLiteral, final int size, final String customCssClass) {
    FontIcon icon = create(iconLiteral, size);
    icon.getStyleClass().add(customCssClass);
    return icon;
  }
}
