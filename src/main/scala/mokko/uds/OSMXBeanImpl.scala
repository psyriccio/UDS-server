/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import java.lang.management.ManagementFactory

trait OSMXBeanImpl {

  def getArch(): String = {
    ManagementFactory.getOperatingSystemMXBean.getArch()
  }

  def getAvailableProcessors(): Int = {
    ManagementFactory.getOperatingSystemMXBean.getAvailableProcessors()
  }
  
  def getOSName(): String = {
    ManagementFactory.getOperatingSystemMXBean.getName()
  }

  def getSystemLoadAverage(): Double = {
    ManagementFactory.getOperatingSystemMXBean.getSystemLoadAverage()
  }
  
  def getOSVersion(): String = {
    ManagementFactory.getOperatingSystemMXBean.getVersion()
  }
  
}
