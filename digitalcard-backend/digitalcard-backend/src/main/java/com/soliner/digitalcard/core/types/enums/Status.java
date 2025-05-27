package com.soliner.digitalcard.core.types.enums;

/**
* Uygulama genelinde kullanılan bir durum (status) enum'u.
* Bu enum, herhangi bir katmanda kullanılabilir ve HTTP'ye bağımlı değildir.
*/
public enum Status {
   ACTIVE("Aktif"),
   INACTIVE("Pasif"),
   PENDING("Beklemede"),
   DELETED("Silindi");

   private final String description;

   Status(String description) {
       this.description = description;
   }

   public String getDescription() {
       return description;
   }
}
