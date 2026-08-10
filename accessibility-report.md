# FlushID Accessibility Report

## 1. Principles of Universal Design

### Equitable Use

Guests and account holders receive the same essential washroom, review, route, filter, sorting, and live-status information. Accounts add optional personalization and review ownership without hiding core campus information, while moderator-only destructive actions are restricted to protect every user's safety and data integrity. Reviews are anonymous to other viewers to protect users from discrimination. Every user has the same level of privacy, security, and safety. All passwords are hashed, and only info stored about users is used to improve their own UX.

### Flexibility in Use

Users can find a destination by selecting the map or entering an address, then narrow results with independent filters and sorting choices. Large, spaced controls support pointing accuracy, but keyboard navigation and configurable text scaling should be added so mouse use is not the only efficient interaction method.

### Simple and Intuitive Use

Consistent labels, Back/Cancel actions, and synchronized map-and-list selection make the major workflows predictable. There are no time sensitive actions, so users can go as slow as they like. Filters, sorts, reviews, and route results are shown near the washroom context they affect; a future onboarding hint should explain less discoverable gestures such as changing the starting location.

### Perceptible Information

Washroom location is presented redundantly through a labelled list (text-based) and a synchronized map (pictoral), and clicking on a washroom on either one will focus the other onto the selected washroom. Live status is available as text rather than by colour alone. Route distance and time, chart labels, heatmap legends, validation messages, and confirmation text provide additional non-colour cues; screen-reader labels and text alternatives remain important future work.

### Tolerance for Error

Input is validated before account changes or generated plans are persisted, and Back/Cancel actions let users leave workflows without committing changes. Destructive moderator actions are authorization-checked and refresh the queue after completion; confirmation dialogs and an undo window would further reduce the effect of accidental deletion. Buttons are made fairly large, with sufficient space between them; users with impaired fine motor skills or high mouse sensitivity can use them with little difficulty.

### Low Physical Effort

Common tasks—selecting a washroom, changing a sort, applying a filter, and opening reviews—require only a small number of actions. The current mouse-heavy interface can still create effort for some users, so keyboard shortcuts, focus traversal, and fewer precision-dependent interactions are priorities.

### Size and Space for Approach and Use

Buttons are relatively large and separated, reducing accidental activation for users with limited fine-motor precision. The desktop layout does not yet scale well to small or high-DPI displays, so responsive panels, resizable text, and testing at multiple scaling settings are needed.

## 2. Target Audience

FlushID is intended for students, faculty, staff, and visitors who spend time on the University of Toronto St. George campus. Students are a key audience because timetable-based planning can recommend convenient washrooms between scheduled classes. Visitors also benefit because they may not know building locations, accessibility details, or current conditions. A future multi-campus or public-washroom dataset could broaden the audience to anyone who needs reliable washroom information.

## 3. Demographics That May Be Excluded

The current interface may create disability-related exclusion for people who use screen readers, keyboard-only navigation, large text, high-contrast themes, or speech input because those modes are not fully supported. Small fixed-layout text and colour-heavy visualizations can also create barriers for people with low vision, colour-vision differences, or light sensitivity. Requiring a desktop device, pointing hardware, and a stable internet connection can reinforce the digital divide for people with limited or inconsistent access to technology. These are barriers produced by the interaction between the software artifact and the user's environment—not deficits in the user—and should guide future accessibility testing with affected communities. 
