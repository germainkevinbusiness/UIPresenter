# UIPresenter

A library to present android UI elements. The library is still under development

## Here is an example of a Squircle-shaped Presenter

The purple background rectangle with the text is displayed using this library.
<img src="/screenshots/device-2021-12-08-095612.png" alt="A Squircle-shaped Presenter View" width="360" />
<img src="/screenshots/device-2021-12-08-095720.png" alt="A Squircle-shaped Presenter View" width="360" />

## Usage (might change in the future)

This library is only functional when called from a class that is either an Activity, a Dialog or a
Fragment.

Basic usage is shown below, when you want to present a View with this library.

In this case, when you want to set a background color, text, text color:

```kotlin
private val mFirstColor by lazy { ContextCompat.getColor(this, R.color.purple_700) }

UIPresenter(this)
    .setViewToPresent(R.id.tvListCounter)
    .setBackgroundColor(mFirstColor)
    .setDescriptionText(descriptionText)
    .setDescriptionTextColor(mThirdColor)
    .setAutoRemoveOnClickEvent(false)
    .setPresenterStateChangeListener { state, removePresenter ->
        // check if the user has pressed on the presenter
        if (state == Presenter.STATE_FOCAL_PRESSED) {
            // use this to remove the presenter from the UI
            removePresenter(Unit)
            showMenuItem()
        }
    }
    .present()
```

Or if you just want to specify even more values such as text size, typeface,shadow layer, animation,
animation duration, listening to state changes:

```kotlin
private val mFirstColor by lazy { ContextCompat.getColor(this, R.color.purple_700) }
private val mSecondColor by lazy { ContextCompat.getColor(this, R.color.blue_500) }
private val mThirdColor by lazy { ContextCompat.getColor(this, R.color.white) }

UIPresenter(fragment = this)
    .setViewToPresent(binding.mListItemCountTv)
    .setBackgroundColor(mFirstColor)
    .setDescriptionText("This text tells you how many animal names are displayed in the below list. There are 7 days in a week, the first one being Monday, the second one is Tuesday, the third one is Wednesday")
    .setDescriptionTextColor(mThirdColor)
    .setDescriptionTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    .setDescriptionTypeface(Typeface.DEFAULT)
    .setHasShadowLayer(true)
    .setShadowLayerColor(mSecondColor)
    .setRevealAnimation(RevealAnimation.ROTATION_Y)
    .setRevealAnimationDuration(1000L)
    .setAutoRemoveOnClickEvent(false)
    .setPresenterStateChangeListener { state, removePresenter ->
        if (state == Presenter.STATE_FOCAL_PRESSED) {
            removePresenter(Unit)
        }
    }
    .present()
```

## License

Licenced under the MIT Licence

```
Copyright (c) 2021 Kevin Germain

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```