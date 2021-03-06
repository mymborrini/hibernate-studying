package model;

import javax.persistence.AttributeOverrides;
import javax.persistence.Column;

import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import freemarker.core.ReturnInstruction;

@Entity
@Table(name = "USER")
public class User {

  @Id
  private Long id;

  private String firstName;
  private String lastName;
  private String username;
  private String password;
  private String email;
  private int ranking;
  private boolean admin;
  private static Preference preferences = new Preference(Currency.getInstance("EUR"));

  /**
   * Here i have to ovveride columns since home Address and billing Address have
   * different column name
   */
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "street", column = @Column(name = "HOME_STREET")),
      @AttributeOverride(name = "zipcode", column = @Column(name = "HOME_ZIPCODE")),
      @AttributeOverride(name = "city", column = @Column(name = "HOME_CITY")) })
  private Address homeAddress;

  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "street", column = @Column(name = "BILLING_STREET")),
      @AttributeOverride(name = "zipcode", column = @Column(name = "BILLING_ZIPCODE")),
      @AttributeOverride(name = "city", column = @Column(name = "BILLING_CITY")) })
  private Address billingAddress;

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public int getRanking() {
    return ranking;
  }

  public boolean isAdmin() {
    return admin;
  }

  public Address getHomeAddress() {
    return homeAddress;
  }

  public Address getBillingAddress() {
    return billingAddress;
  }

  public static Preference getPreferences() {
    return preferences;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public void setHomeAddress(Address homeAddress) {
    this.homeAddress = homeAddress;
  }

  public void setBillingAddress(Address billingAddress) {
    this.billingAddress = billingAddress;
  }

  public User() {
  }

  // Generally an hibernate custom type is better to handle this
  /*
   * public String getName() { return firstName + " " + lastName; }
   * 
   * public void setName(String name) { StringTokenizer t = new
   * StringTokenizer(name); firstName = t.nextToken(); lastName = t.nextToken(); }
   */

  /**
   * 
   */
  /*
   * @OneToOne
   * 
   * @PrimaryKeyJoinColumn private AddressEntity shippingAddress;
   */

  public AddressEntity getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(AddressEntity shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  @OneToOne
  @JoinColumn(name = "SHIPPING_ADDRESS_ID")
  private AddressEntity shippingAddress;

  @OneToMany(mappedBy = "buyer")
  private Set<Item> boughtItems = new HashSet<>();

  /**
   * Let's see polymorphic association with billing details In this case the
   * billingDetail property of a user it refers one particular BillingDetails
   * object which at runtime can be any concrete instance of that class (Credi
   * card or bank account).
   * 
   * It's enough to enable polymorphic declare a many to one and hibernate will do
   * the rest (in case of <union-subclass> <subclass> <joined-subclass>).
   * 
   * CrediCard cc = new CreditCard(); cc.setNumber(ccNumber);....
   * 
   * User user = (User) session.get(User.class, userId);
   * user.setBillingDetails(cc);
   * 
   * When you navigate in the association in a second unit of work hibernate
   * automatically retrieves the crediCard instance
   * 
   * User user = (User) session.get(User.class, userId);
   * user.getDefaultBillingDetails().pay();
   * 
   * The problem here would be with fetch Lazy, in this case
   * user.getBillingDetails() will return a proxy instance so even the cast to
   * credit card would fail. So you have to fetch EAGER some how maybe with load
   * method like this:
   * 
   * User user = (User) session.get(User.class, userId); BillingDetails bd =
   * user.getDefaultBillingDetails().pay(); CreditCard cc = (CreditCard)
   * session.load(CreditCard.class, bd.getId());
   * 
   * cc is a different proxy instance where you can call methods
   * 
   * Now what about a polymorphic collections, if it's mapped like table per clazz
   * hierarchy or table per subclass everything is fine, the behavoiur is the
   * same, as normal association.
   * 
   * However if the mapping strategy is table per concrete class (impolicit
   * polymorphis) or table with concrete class per union this scenario requires a
   * more sophisticated solutions both in many to one and in to many.
   * 
   * For HIERARCHY PER CLASS UNION feature, a requirement for this polymorphic
   * association is that must be an inverse so you have to include a many to one
   * association. Each concrete table must have a user_id foreign key
   * 
   * In this case is enough to select from the union all the rows where user_id is
   * the one specified . This work great for retrieving data. And in case of
   * manipulating collections since exist an inverse true only the modification in
   * BillingDetails will take affects normally so in the instance of the classes.
   * So you perist a concrete class with a user since the billingDetails
   * collections which contains a bag of abstract class is ignored.
   * 
   * In case of the many to one it follow the same principle since there is an
   * union the where clause will refer to the billing details id
   * 
   * In case of TABLE_PER_CONCRETE_CLASS strategy or implicit polymorphic only a
   * one to many is supported and it needs to column, one for the type and the
   * other one for the identifier. The only way to do it is using meta-value a
   * feature not supported in javax.persistence
   * 
   * in an xml format it is shown like this
   * <any name="defaultBillingDetail" id-type="long" meta-type="string">
   * <meta-value value="CC" class="CrediCard"/><meta-value value="BA" class=
   * "BankAccount"/> <column name="DEFAULT_BILLING_TYPE" />
   * <column name="DEFAULT_BILLING_ID" /> </any>
   * 
   */

  @ManyToOne
  private BillingDetails defaultBillingDetail;

  public BillingDetails getDefaultBillingDetail() {
    return defaultBillingDetail;
  }

  @OneToMany(mappedBy = "user")
  @JoinColumn(name = "USER_ID")
  private Set<BillingDetails> billingDetails;

  /**
   * Let's see how to implement equals and hashcode. A clever approch is to
   * implemant equals to compare just the database identifier property (often a
   * surrogate primary key value). But this contains a problem, hibernate does not
   * set an identifier until an object become peristent. If a transient object is
   * added to set before being saved its hash value may change while it's
   * contained by the set, contrary to the contract of java.util.Set. In
   * particular this problem makes cascade save useless for sets. So database
   * identifier equality is not the correct solution
   * 
   * A better way is to include all not null persistent properties apart the
   * database identifier and collections, firstable because they belong to
   * different table and second because you don't want to retrieve the entire
   * object graph to be retrieved just to perform an equals. But this contains two
   * main problem, the same db instance can be different between two session and
   * two different instance may be equals because username and password are the
   * same casually
   * 
   * EQUALITY WITH BUISINESS KEY: A business key is a property. or some
   * combinations of propery that is unique for each instance with the same
   * database identity. Essentialy is the natural key that you would use if you
   * didn't use the surrogate primary key instead. For example for User class it
   * will be the username which is unique, and rarely changes In equals other use
   * ALWAY getter method, this is really important because the object instace
   * passed as other may be a proxy object not an actual instace that holds the
   * persistence state
   */

  public boolean equalsDbIdentifier(Object other) {
    if (this == other)
      return true;
    if (id == null)
      return false;
    if (!other.getClass().equals(this.getClass()))
      return false;
    final User that = (User) other;
    return this.id.equals(that.getId());
  }

  public int hashCodeDbIdentifier() {
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  public boolean equalsAllProperties(Object other) {
    if (this == other)
      return true;
    if (!other.getClass().equals(this.getClass()))
      return false;

    final User that = (User) other;
    if (!this.getUsername().equals(that.getUsername())) {
      return false;
    }

    if (!this.getPassword().equals(that.getPassword())) {
      return false;
    }

    return true;

  }

  public int hashCodeAllProperties() {
    final int prime = 29;
    int result = 14;
    result = prime * result + getUsername().hashCode();
    result = prime * result + getPassword().hashCode();
    return result;
  }

  public boolean equalsBusinessKey(Object other) {
    if (this == other)
      return true;
    if (!other.getClass().equals(this.getClass()))
      return false;

    User that = (User) other;

    if (!this.username.equals(that.getUsername())) {
      return false;
    }
    return true;
  }

  public int hashCodeBusinessKey() {
    return username.hashCode();
  }

}
