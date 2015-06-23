/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import java.lang.management.ManagementFactory

trait RuntimeMXBeanImpl {

  def getVMName(): String = {
    ManagementFactory.getRuntimeMXBean.getName()
  }

  def getVMSpecName(): String = {
    ManagementFactory.getRuntimeMXBean.getSpecName()
  }

  def getVMSpecVendor(): String = {
    ManagementFactory.getRuntimeMXBean.getSpecVendor()
  }

  def getVMSpecVersion(): String = {
    ManagementFactory.getRuntimeMXBean.getSpecVersion()
  }

  def getVMImplName(): String = {
    ManagementFactory.getRuntimeMXBean.getVmName()
  }
  
  def getVMImplVendor(): String = {
    ManagementFactory.getRuntimeMXBean.getVmVendor()
  }
  
  def getVMImplVersion(): String = {
    ManagementFactory.getRuntimeMXBean.getVmVersion()
  }
  
}
