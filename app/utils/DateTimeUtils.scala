package utils

import java.sql.Timestamp
import java.text.{DateFormat, DecimalFormat, SimpleDateFormat}
import java.util.concurrent.TimeUnit
import java.util.{Calendar, Date, Locale}

import constants.DateConstants
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

object DateTimeUtils extends DateConstants {

  val logger, log = LoggerFactory.getLogger(getClass)


  /**
    * Returns the current date
    * in yyyymmdd format
    */
  def getDateInYYYYMMDD(): String = {

    val dF = new DecimalFormat("00")
    val dateTime = new DateTime
    val dateValue: StringBuilder = new StringBuilder

    dateValue.append(dateTime.getYear)
    dateValue.append(dF.format(dateTime.getMonthOfYear))
    dateValue.append(dF.format(dateTime.getDayOfMonth))

    dateValue.toString

  }

  /**
    * Returns number of years between two dates
    *
    * @param first
    * @param last
    * @return
    */
  def getDiffYears(first: Date, last: Date): Int = {
    val a = getCalendar(first)
    val b = getCalendar(last)
    var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
    if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
      (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE) + 1)) {
      diff = diff - 1
    }
    diff
  }

  def getDiffDays(past: Date, future: Date): Long = {
    val diff = future.getTime() - past.getTime()
    TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
  }

  /**
    * takes date input in format yyyy-MM-dd and returns java.sql.date
    *
    * @param dateString
    * @return
    */
  def yyyyMMddToSQLDate(dateString: String): java.sql.Date = {
    val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat(YYYYMMDD);
    val date: Date = simpleDateFormat.parse(dateString);
    new java.sql.Date(date.getTime());
  }

  def convertCalendarToSqlDate(calendar: Calendar) = {
    new java.sql.Date(calendar.getTimeInMillis)
  }

  def getCurrentTimeStamp() = {
    new java.sql.Timestamp(Calendar.getInstance.getTimeInMillis)
  }

  def getNthDayDate(noOfDays: Int): Date = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, noOfDays)
    calendar.getTime
  }

  def getCurrentTimeStampString(format: Option[String]): String = {
    val outputFormat = format.getOrElse(YYYYMMDDHHMMSS);
    val dateFormat: DateFormat = new SimpleDateFormat(outputFormat);
    val cal: Calendar = Calendar.getInstance();
    dateFormat.format(cal.getTime());

  }

  def getTimestampAfterInterval(mins: Int) = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, mins)
    new java.sql.Timestamp(calendar.getTimeInMillis)
  }

  def getTimestampAfterDays(days: Int) = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, days)
    new java.sql.Timestamp(calendar.getTimeInMillis)
  }

  private def getCalendar(date: Date): Calendar = {
    val cal = Calendar.getInstance(Locale.US);
    cal.setTime(date);
    return cal
  }

  def convertStringDateToDDMMYY(date: String): Date = {
    new SimpleDateFormat(DDMMYY).parse(date)
  }

  def convertStringYYYYMMDDDateToDDMMYYYY(date: String): String = {
    val retval = convertStringDateWithFormats(date, YYYYMMDD, BSE_CLIENT_DOB_FORMAT)
    if (retval.isDefined) {
      retval.get
    } else {
      throw new Exception("Date Parsing Error > cant parse " + date + " from " + YYYYMMDD + " to " + BSE_CLIENT_DOB_FORMAT);
    }
  }

  def convertDateToYYYYMMDD(date: Date): String = {
    new SimpleDateFormat(YYYYMMDD).format(date)
  }

  def convertDateToFormat(date: Date, format: String): String = {
    new SimpleDateFormat(format).format(date)
  }

  def convertDateToFormat(date: String, format: String): Option[Date] = {
    try {
      Some(new SimpleDateFormat(format).parse(date))
    } catch {
      case e: Exception => logger.error("{}",e)
        None
    }
  }

  def convertStringDateWithFormats(date: String, fromformat: String, toformat: String): Option[String] = {
    try {
      Some(new SimpleDateFormat(toformat).format(new SimpleDateFormat(fromformat).parse(date)))
    } catch {
      case e: Exception => logger.error("{}", e)
        None
    }
  }

  def getCurrentDate(): Date = {
    convertDateToFormat(new SimpleDateFormat(YYYYMMDD).format(new Date()), YYYYMMDD).get
  }

  def getFinancialYear(offset: Int): (Date, Date) = {

    val calendar = Calendar.getInstance()

    if (calendar.get(Calendar.MONTH) > 2) {
      calendar.add(Calendar.YEAR, 1)
    }

    calendar.set(Calendar.MONTH, 2)
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.add(Calendar.YEAR, -offset)
    val endDate = calendar.getTime

    calendar.set(Calendar.MONTH, 3)
    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
    calendar.add(Calendar.YEAR, -1)
    val startDate = calendar.getTime

    (startDate, endDate)
  }


  def getEstimatedSIPDate(dayOfMonth: Int): String = {

    val calendar = Calendar.getInstance()
    while (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) < dayOfMonth) {
      calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
    }
    calendar.set(Calendar.DATE, dayOfMonth)
    val currentDate = Calendar.getInstance()
    var daysDiff = (calendar.getTime.getTime - currentDate.getTime.getTime) / (1000 * 60 * 60 * 24) + 1

    while (daysDiff < 15) {
      calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
      daysDiff = (calendar.getTime.getTime - currentDate.getTime.getTime) / (1000 * 60 * 60 * 24) + 1
    }
    new SimpleDateFormat(BSE_SIP_DATE_FORMAT).format(calendar.getTime)
  }

  def checkBsePassTimeValidity(time: java.sql.Timestamp): Boolean = {

    val expiryTime = (time.getTime) / 1000
    val currTime = Calendar.getInstance().getTimeInMillis / 1000

    if (expiryTime - currTime > 60) {
      true
    } else {
      false
    }
  }

  def convertCalendarToSqlTimestamp(calendar: Calendar): Timestamp = {
    new Timestamp(calendar.getTimeInMillis)
  }

  def convertSqlTimestampToString(time: java.sql.Timestamp) = {

    val date = new Date()
    val timeInMillis = time.getTime
    date.setTime(timeInMillis)
    val dateFormat: DateFormat = new SimpleDateFormat(YYYYMMDDHHMMSS);
    dateFormat.format(date);
  }

  def setTimeToEOD(date: Date) = {
    val cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    cal.getTime
  }

  def setTimeToBOD(date: Date) = {

    val cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 1);
    cal.set(Calendar.MILLISECOND, 0);
    cal.getTime
  }

  def setTimeToEOD(timeStamp: java.sql.Timestamp) = {
    val cal = Calendar.getInstance();
    cal.setTimeInMillis(timeStamp.getTime);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
    cal.getTime
  }

  def setTimeToBOD(timestamp: java.sql.Timestamp) = {
    val cal = Calendar.getInstance();
    cal.setTimeInMillis(timestamp.getTime);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 1);
    cal.set(Calendar.MILLISECOND, 0);
    cal.getTime
  }

  def getMaxMinFromTimeList(timeList: List[java.sql.Time], isMaxRequired: Option[Boolean] = None, isMinRequired: Option[Boolean] = None): java.sql.Time = {
    val minTime = timeList.head;
    var currDate = new Date();
    currDate.setTime(minTime.getTime)
    timeList.foreach(time => {
      val checkTime = new Date(time.getTime)
      if (checkTime.before(currDate) && isMinRequired.isDefined && isMinRequired.get) {
        currDate = checkTime
      } else if (checkTime.after(currDate) && isMaxRequired.isDefined && isMaxRequired.get) {
        currDate = checkTime
      }
    })
    new java.sql.Time(currDate.getTime)
  }

  def get12HourStringFromTime(time: java.sql.Time): String = {
    var cutOffTime = "";
    val timeString = time.toString
    val timeArray = timeString.split(":");
    var amPm = "AM";
    if (timeArray(0).toInt > 12) {
      cutOffTime = (timeArray(0).toInt - 12) + "";
      amPm = "PM";
    } else {
      cutOffTime = timeArray(0)
    }
    cutOffTime += ":" + timeArray(1) + " " + amPm;
    cutOffTime
  }

  def isGreaterThanCurrentDate(date: Date): Boolean = {
    val currCal = Calendar.getInstance();
    currCal.getTime.before(date)
  }
}