package validator

import models.ErrorModel

trait BaseValidator[T] {
   
  def validate(t:T):Seq[ErrorModel]
}