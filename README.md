# UIPresenter

A library to present android UI elements. The library is still under development


## Usage (might change in the future)
This library is only functional when called from a class that is either an Activity, a Dialog or a Fragment.

Basic usage is shown below, when you want to present a View with this library.

In this case, when you want to set a background color, text, text color:
```kotlin
private val mFirstColor by lazy { ContextCompat.getColor(this, R.color.purple_700) }

UIPresenter(activity = this)
            .setViewToPresent(binding.mHelloWorld)
            .setBackgroundColor(mFirstColor)
            .setDescriptionText("This is a TextView, its role is to display a Hello World text at the top of the screen")
            .setDescriptionTextColor(mThirdColor)
            .present()
```

Or if you just want to specify even more values such as text size, typeface,shadow layer, animation, animation duration, listening to state changes:
```kotlin
private val mFirstColor by lazy { ContextCompat.getColor(this, R.color.purple_700) }
private val mSecondColor by lazy { ContextCompat.getColor(this, R.color.blue_500) }
private val mThirdColor by lazy { ContextCompat.getColor(this, R.color.white) }

UIPresenter(this)
            .setViewToPresent(binding.mHelloWorld)
            .setBackgroundColor(mFirstColor)
            .setDescriptionText("This is a TextView, its role is to display text, like the one seen here")
            .setDescriptionTextColor(mThirdColor)
            .setDescriptionTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            .setDescriptionTypeface(Typeface.DEFAULT)
            .setHasShadowLayer(true)
            .setShadowLayerColor(mSecondColor)
            .setPresenterAnimation(Presenter.ANIM_CIRCULAR_REVEAL)
            .setAnimationDuration(1000L)
            .setPresenterStateChangeListener { state ->
                Timber.d("state: $state")
                if (state == Presenter.STATE_NON_FOCAL_PRESSED) {
                    showListViewItem()
                }
            }
            .present()
```
