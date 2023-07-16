package mini.swipe.uistate

data class AddNewProdUIState(val updateImgBtnTxt : String ?= null,
                             val toUpdateImgBtn : Boolean = false,
                             val prodAddSuccess:Boolean = false,
                             val displayProgressBar : Boolean = false)
