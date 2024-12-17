package com.testdemo.dsaapp

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.util.Arrays
import java.util.Collections
import java.util.concurrent.Executors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {
    private val TAG="MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Executors.newSingleThreadExecutor().execute(Runnable {
            Log.e(TAG, "onCreate: Executors")
        })
        /**
         * Leepcode
         */
        /**
         * Given an integer array arr, return true if there are three consecutive odd numbers in the array. Otherwise, return false
         * Input: arr = [1,2,34,3,4,5,7,23,12]
         * Output: true
         * Explanation: [5,7,23] are three consecutive odds.
         */
        val arr = intArrayOf(1,2,34,3,4,5,7,23,12)
        Log.e(TAG, "Sum Status: "+threeConsecutiveOdds(arr))

        /**
         * You may assume that each input would have exactly one solution, and you may not use the same element twice.
         *
         */
        val nums = intArrayOf(11, 5, 10, 4)
        val target = 9
        val result: IntArray = twoSum(nums, target)
        Log.e(TAG, "Sum Num Indices: " + result[0] + ", " + result[1])


        //Todo find the length of the longest substring without repeating characters.
        val str = "abcabcbb"
        Log.e(TAG, "lengthOfLongestSubstring: " + lengthOfLongestSubstring(str))
        Log.e(TAG, "lengthOfLongestSubstring 2: " + lengthOfLongestSubstringWithoutMap(str))

        /**
         * HackerRank
         */

             /**
             * Complete the 'plusMinus' function below with Proportion अनुपात.
             * The function accepts INTEGER_ARRAY arr as parameter.
             */
        val ns = intArrayOf(-1,-5,0, 10, 4)
        plusMinus(ns)

        /**
         * Given five positive integers,
         * find the minimum and maximum values that can be calculated by summing exactly four of the five integers.
         * Then print the respective minimum and maximum values as a single line of two space-separated long integers.
         */
        val arr1= intArrayOf(256741038,623958417,467905213,714532089,938071625)
        miniMaxSumWithSpace(arr1)
        /**
             * Complete the 'timeConversion' function below.
             *
             * The function is expected to return a STRING.
             * The function accepts STRING s as parameter.
             */
        Log.e(TAG, "timeConversion: "+timeConversion("12:00:00AM"))
        Log.e(TAG, "timeConversion: "+timeConversion("12:00:00PM"))
        Log.e(TAG, "timeConversion: "+timeConversion("12:00:00PM"))





        startActivity(Intent(this@MainActivity,DownloadFileActivity::class.java))

      //  Log.e(TAG, "Lat Long: "+calculateDistance(28.6271939,77.3740726,28.6264936,77.3718924))
       // Log.e(TAG, "Lat Long2: "+calculateDistance2(28.6271939,77.3740726,28.6264936,77.3718924))
    }
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lon1 = lon1
        var lat2 = lat2
        var lon2 = lon2
        val R = 6371.0
        lat1 = Math.toRadians(lat1)
        lon1 = Math.toRadians(lon1)
        lat2 = Math.toRadians(lat2)
        lon2 = Math.toRadians(lon2)
        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = sin(dlat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c
        return distance * 1000
    }
    fun calculateDistance2(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val startLocation = Location("start")
        startLocation.setLatitude(startLat)
        startLocation.setLongitude(startLng)

        val endLocation = Location("end")
        endLocation.setLatitude(endLat)
        endLocation.setLongitude(endLng)
        return startLocation.distanceTo(endLocation)
    }
    fun threeConsecutiveOdds(arr: IntArray): Boolean {
        for (i in 0 until arr.size - 2) {
            if (arr[i] % 2 != 0 && arr[i + 1] % 2 != 0 && arr[i + 2] % 2 != 0) {
                return true
            }
        }
        return false
    }
    fun twoSum(nums: IntArray, target: Int): IntArray {
        val map: MutableMap<Int, Int> = HashMap()
        for (i in nums.indices) {
            val complement = target - nums[i]
            if (map.containsKey(complement)) {
                return intArrayOf(map[complement]!!, i)
            }
            map[nums[i]] = i
        }
        return intArrayOf(-1, -1)
    }

    fun lengthOfLongestSubstring(s: String): Int {
        var maxLength = 0
        if (s.isEmpty()) {
            return maxLength
        }
        val seen: MutableMap<Char, Int> = HashMap()
        var left = 0
        for (i in 0 until s.length) {
            val currentChar = s[i]
            if (seen.containsKey(currentChar) && seen[currentChar]!! >= left) {
                left = seen[currentChar]!! + 1
            }
            seen[currentChar] = i
            maxLength = max(maxLength.toDouble(), (i - left + 1).toDouble()).toInt()
        }
        return maxLength
    }
    fun lengthOfLongestSubstringWithoutMap(s: String): Int {
        var maxLength = 0
        if (s.isEmpty()) {
            return maxLength
        }
        val lastOccurrence = IntArray(128) // Assuming ASCII characters
        Arrays.fill(lastOccurrence, -1) // Initialize all characters' last occurrences to -1
        var left = 0
        for (i in 0 until s.length) {
            val currentChar = s[i]
            val lastSeen = lastOccurrence[currentChar.code]
            if (lastSeen != -1 && lastSeen >= left) {
                left = lastSeen + 1
            }
            lastOccurrence[currentChar.code] = i
            maxLength = max(maxLength.toDouble(), (i - left + 1).toDouble()).toInt()
        }
        return maxLength
    }



    fun plusMinus(arr: IntArray) {
        var zz = 0
        var even = 0
        var odd = 0
        for (i in arr.indices) {
            if (arr[i] == 0) {
                   zz++
            } else {
                if (arr[i] > 0) {
                    even++
                }
                if (arr[i] < 0) {
                    odd++
                }
            }
        }
        val df = DecimalFormat("0.000000")
        System.out.println("Proportion of zero elements: " + df.format(zz.toDouble() / arr.size))
        System.out.println("Proportion of even elements: " + df.format(even.toDouble() / arr.size))
        System.out.println("Proportion of odd elements: " + df.format(odd.toDouble() / arr.size))
    }
    private fun plusMinus(arr: List<Int>) {
        val n = arr.size
        var plus = 0f
        var minus = 0f
        var zero = 0f
        for (i in 0 until n) {
            if (arr[i] == 0) {
                zero++
            } else if (arr[i] > 0) {
                plus++
            } else if (arr[i] < 0) {
                minus++
            }
        }
        Log.e(TAG, "plusMinus: " + plus / n)
        Log.e(TAG, "plusMinus2: " + minus / n)
        Log.e(TAG, "plusMinus3: " + zero / n)
    }

  private fun miniMaxSumWithSpace(arr: IntArray){
      if (arr.isEmpty()) {
          throw IllegalArgumentException("Array must not be empty")
      }
      var min = arr[0]
      var max = arr[0]
      for (i in 1 until arr.size) {
          if (arr[i] < min) {
              min = arr[i]
          }
          if (arr[i] > max) {
              max = arr[i]
          }
      }
      var totalSum: Long = 0
      for (num in arr) {
          totalSum += num
      }
      val minSum = totalSum - max
      val maxSum = totalSum - min
     print("$minSum $maxSum")
  }
    private fun miniMaxSum(arr: List<Int>) {
        var totalSum: Long = 0
        for (items in arr) {
            totalSum += items.toLong()
        }
        val minSum = totalSum - Collections.max(arr)
        val maxSum = totalSum - Collections.min(arr)
        Log.e(TAG, "Min Sum: $minSum")
        Log.e(TAG, "Max Sum: $maxSum")
    }
    @SuppressLint("DefaultLocale")
    fun timeConversion(s: String): String {
        // Write your code here
        var hour = s.substring(0, 2).toInt()
        val minutes = s.substring(3, 5).toInt()
        val seconds = s.substring(6, 8).toInt()
        val period = s.substring(8, 10)
        if (period == "PM" && hour < 12) {
            hour += 12
        } else if (period == "AM" && hour == 12) {
            hour = 0
        }
        return  String.format("%02d", hour) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds)
    }



    fun isPalindrome(txt:String): Boolean {
        val cleaned = txt.replace("\\s".toRegex(), "").lowercase()
        return cleaned == cleaned.reversed()
    }



}